package fr.memoires_vives.bll;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Role;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.FileStorageException;
import fr.memoires_vives.exception.UnauthorizedActionException;
import fr.memoires_vives.exception.ValidationException;
import fr.memoires_vives.repositories.RoleRepository;
import fr.memoires_vives.repositories.UserRepository;
import fr.memoires_vives.security.CustomUserDetails;
import fr.memoires_vives.security.CustomUserDetailsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final FileService fileService;
	private final CustomUserDetailsService customUserDetailsService;

	@PersistenceContext
	private EntityManager entityManager;

	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
			RoleRepository roleRepository, FileService fileService, CustomUserDetailsService customUserDetailsService) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.fileService = fileService;
		this.customUserDetailsService = customUserDetailsService;
	}

	@Override
	public User getUserByPseudo(String pseudo) {
		User user = userRepository.findByPseudo(pseudo)
				.orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
		return user;
	}

	@Override
	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	@Override
	public User getUserById(long userId) {
		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé pour l'ID " + userId));
		Hibernate.initialize(user.getFriends());
		Hibernate.initialize(user.getMemories());
		return user;
	}

	@Override
	@Transactional(readOnly = true)
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
			return null;
		}
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		User user = userRepository.findByUserId(userDetails.getUser().getUserId()).orElse(null);
		if (user == null) {
			return null;
		}
		Hibernate.initialize(user.getMemories());
		entityManager.detach(user);
		user.setPassword(null);
		return user;
	}

	@Override
	public User getUserOrCurrent(Long userId) {
		if (userId == null || userId == 0) {
			return getCurrentUser();
		}
		return getUserById(userId);
	}

	@Override
	@Transactional
	public User createAccount(String pseudo, String email, String password, String passwordConfirm,
			MultipartFile image) {
		ValidationException ve = new ValidationException();

		validateAccountInputs(pseudo, email, password, passwordConfirm, ve);

		User user = buildUser(pseudo, email, passwordConfirm);

		assignUserRole(user);

		if (ve.hasError()) {
			throw ve;
		}

		saveProfileImage(user, image, true);

		return persistUser(user, ve);
	}

	@Override
	@Transactional
	public User updateProfile(User updatedData, String currentPassword, MultipartFile fileImage) {
		ValidationException ve = new ValidationException();

		User currentUser = getCurrentUser();
		User userToUpdate = getUserById(updatedData.getUserId());

		if ((!isAdmin() && currentUser.getUserId() != userToUpdate.getUserId())) {
			throw new UnauthorizedActionException("Vous n'avez pas la permission de modifier ce profil");
		}

		validatePassword(userToUpdate, currentPassword, ve);

		updateProfileFields(userToUpdate, updatedData, ve);

		if (ve.hasError()) {
			throw ve;
		}

		saveProfileImage(userToUpdate, fileImage, false);

		User saved = persistUser(userToUpdate, ve);

		refreshSecurityContext(saved);

		return saved;
	}

	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getAuthorities() != null) {
			return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		return false;
	}

	@Override
	public boolean verifyPassword(String rawPassword) {
		CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		String storedPassword = userDetails.getPassword();
		return passwordEncoder.matches(rawPassword, storedPassword);
	}

	@Override
	@Transactional
	public void updatePassword(User user, String rawPassword) {
		ValidationException ve = new ValidationException();
		Long userId = user.getUserId();
		if (rawPassword == null || rawPassword.isBlank()) {
			ve.addFieldError("currentPassword", "Vous devez renseigner le mot de passe");
			throw ve;
		}
		User managedUser = userRepository.findByUserId(userId)
				.orElseThrow(() -> new EntityNotFoundException("Utilisateurice introuvable."));
		String hashedPassword = passwordEncoder.encode(rawPassword);
		managedUser.setPassword(hashedPassword);
	}

//	Les méthodes privées

	private void validateAccountInputs(String pseudo, String email, String password, String passwordConfirm,
			ValidationException ve) {
		checkPassword(password, passwordConfirm, ve);
		checkPseudoAvailable(pseudo, ve);
		checkEmailAvailable(email, ve);
	}

	private User buildUser(String pseudo, String email, String password) {
		User user = new User();
		user.setPseudo(pseudo);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		user.setAdmin(false);
		user.setActivated(true);
		return user;
	}

	private void assignUserRole(User user) {
		Role userRole = roleRepository.findByName("ROLE_USER");
		if (userRole == null) {
			userRole = new Role();
			userRole.setName("ROLE_USER");
			roleRepository.save(userRole);
		}
		user.getRoles().add(userRole);
	}

	private void saveProfileImage(User user, MultipartFile image, boolean isCreate) {
		if (image == null || image.isEmpty()) {
			if (isCreate) {
				user.setMediaUUID(null);
			}
			return;
		}

		try {
			user.setMediaUUID(fileService.saveUserFile(image, user.getPseudo()));
		} catch (FileStorageException e) {
			if (isCreate) {
				// Création : on met à null si erreur
				user.setMediaUUID(null);
			}
			// Update : on laisse l'ancienne image
		}
	}

	private User persistUser(User user, ValidationException ve) {
		try {
			return userRepository.save(user);
		} catch (DataAccessException e) {
			e.printStackTrace();
			ve.addGlobalError("Un problème est survenu lors de l'accès à la base de données.");
			throw ve;
		}
	}

	private void validatePassword(User user, String currentPassword, ValidationException ve) {
		if (currentPassword == null || currentPassword.isBlank()) {
			ve.addFieldError("currentPassword", "Vous devez renseigner le mot de passe.");
		}

		boolean isPasswordValid = (isAdmin() && verifyPassword(currentPassword))
				|| passwordEncoder.matches(currentPassword, user.getPassword());

		if (!isPasswordValid) {
			ve.addFieldError("currentPassword", "Erreur de mot de passe.");
		}
	}

	private void checkPassword(String password, String passwordConfirm, ValidationException ve) {
		if (password.isBlank()) {
			ve.addFieldError("password", "Le mot de passe ne peut pas être vide.");
		}
		if (!password.equals(passwordConfirm)) {
			ve.addFieldError("password", "Les mots de passe ne sont pas identiques.");
		}
	}

	private void checkPseudoAvailable(String pseudo, ValidationException ve) {
		userRepository.findByPseudo(pseudo)
				.ifPresent(user -> ve.addFieldError("pseudo", "Ce pseudo est déjà utilisé."));
	}

	private void checkEmailAvailable(String email, ValidationException ve) {
		if (userRepository.findByEmail(email) != null) {
			ve.addFieldError("email", "Un compte est déjà attaché à cet email.");
		}
	}

	private void updateProfileFields(User user, User updatedData, ValidationException ve) {
		if (!user.getPseudo().equals(updatedData.getPseudo())) {
			checkPseudoAvailable(updatedData.getPseudo(), ve);
			if (!ve.hasError()) {
				user.setPseudo(updatedData.getPseudo());
			}
		}

		if (!user.getEmail().equals(updatedData.getEmail())) {
			checkEmailAvailable(updatedData.getEmail(), ve);
			if (!ve.hasError()) {
				user.setEmail(updatedData.getEmail());
			}
		}

		String newPass = updatedData.getPassword();
		String confirm = updatedData.getPasswordConfirm();
		if (!newPass.isBlank() || !confirm.isBlank()) {
			// si les 2 mots de passe renseignés sont blancs, il n'y a pas de changement de
			// mot de passe et on conserve le mdp de initial
			checkPassword(newPass, confirm, ve);
			if (!ve.hasError()) {
				user.setPassword(passwordEncoder.encode(newPass));
			}
		}
	}

	private void refreshSecurityContext(User user) {
		CustomUserDetails updatedUserDetails = (CustomUserDetails) customUserDetailsService
				.loadUserByUsername(user.getPseudo());
		UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(updatedUserDetails,
				updatedUserDetails.getPassword(), updatedUserDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuth);
	}
}
