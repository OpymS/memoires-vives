package fr.memoires_vives.bll;

import java.io.IOException;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Role;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.BusinessException;
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
	@Transactional(rollbackFor = BusinessException.class)
	public User createAccount(String pseudo, String email, String password, String passwordConfirm, MultipartFile image)
			throws BusinessException {
		BusinessException be = new BusinessException();

		boolean isValid = checkPassword(password, passwordConfirm, be) && checkPseudoAvailable(pseudo, be)
				&& checkEmailAvailable(email, be);

		if (isValid) {
			try {
				User user = new User();
				user.setPseudo(pseudo);
				user.setEmail(email);
				user.setPassword(passwordEncoder.encode(password));
				user.setAdmin(false);
				user.setActivated(true);
				Role userRole = roleRepository.findByName("ROLE_USER");
				if (userRole == null) {
					userRole = new Role();
					userRole.setName("ROLE_USER");
					roleRepository.save(userRole);
				}
				user.getRoles().add(userRole);

				if (image != null && !image.isEmpty()) {
					try {
						user.setMediaUUID(fileService.saveUserFile(image, pseudo));
					} catch (IOException e) {
						e.printStackTrace();
						be.add("Un problème est survenu lors de l'enregistrement de l'image.");
						throw be;
					}
				} else {
					user.setMediaUUID(null);
				}

				return userRepository.save(user);
			} catch (DataAccessException e) {
				e.printStackTrace();
				be.add("Un problème est survenu lors de l'accès à la base de données.");
				throw be;
			}
		} else {
			throw be;
		}
	}

	@Override
	@Transactional(rollbackFor = BusinessException.class)
	public User updateProfile(User userWithUpdate, String currentPassword, MultipartFile fileImage)
			throws BusinessException {
		BusinessException be = new BusinessException();

		boolean isValid = true;

		User userToSave = userRepository.findByUserId(userWithUpdate.getUserId());
		String updatedPseudo = userWithUpdate.getPseudo();
		String currentPseudo = userToSave.getPseudo();

		String updatedEmail = userWithUpdate.getEmail();
		String currentEmail = userToSave.getEmail();

		isValid &= (isAdmin() && verifyPassword(currentPassword))
				|| passwordEncoder.matches(currentPassword, userToSave.getPassword());

		if (currentPassword.isBlank()) {
			be.add("Vous devez renseigner le mot de passe");
			throw be;
		}

		if (!isValid) {
			be.add("Erreur de mot de passe");
			throw be;
		}

		// si les 2 mots de passe renseignés sont blancs, il n'y a pas de changement de
		// mot de passe et on conserve le mdp de userToSave
		if (!userWithUpdate.getPassword().isBlank() || !userWithUpdate.getPasswordConfirm().isBlank()) {
			isValid &= checkPassword(userWithUpdate.getPassword(), userWithUpdate.getPasswordConfirm(), be);
			if (isValid) {
				userToSave.setPassword(passwordEncoder.encode(userWithUpdate.getPassword()));
			}
		}

		if (!updatedPseudo.equals(currentPseudo)) {
			isValid &= checkPseudoAvailable(updatedPseudo, be);
			userToSave.setPseudo(updatedPseudo);
		}

		if (!updatedEmail.equals(currentEmail)) {
			isValid &= checkEmailAvailable(updatedEmail, be);
			userToSave.setEmail(updatedEmail);
		}

		if (fileImage != null && !fileImage.isEmpty()) {
			try {
				userToSave.setMediaUUID(fileService.saveUserFile(fileImage, updatedPseudo));
			} catch (IOException e) {
				e.printStackTrace();
				be.add("Un problème est survenu lors de l'enregistrement de l'image.");
				throw be;
			}
		}

		if (isValid) {
			try {
				userToSave = userRepository.save(userToSave);
				CustomUserDetails updatedUserDetails = (CustomUserDetails) customUserDetailsService
						.loadUserByUsername(userToSave.getPseudo());
				UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
						updatedUserDetails, updatedUserDetails.getPassword(), updatedUserDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(newAuth);
				return userToSave;

			} catch (DataAccessException e) {
				e.printStackTrace();
				be.add("Un problème est survenu lors de l'accès à la base de données.");
				throw be;
			}
		} else {
			throw be;
		}
	}

	@Override
	public User getUserByPseudo(String pseudo) {
		User user = userRepository.findByPseudo(pseudo);

		if (user == null) {
			throw new UsernameNotFoundException("Utilisateur non trouvé");
		}
		return user;
	}

	@Override
	public User getUserByEmail(String email) {
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new UsernameNotFoundException("Utilisateur non trouvé");
		}
		return user;
	}

	@Override
	public User getUserById(long userId) {
		User user = userRepository.findByUserId(userId);
		if (user == null) {
			throw new UsernameNotFoundException("Utilisateur non trouvé");
		}
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
		User user = userRepository.findByUserId(userDetails.getUser().getUserId());
		if (user == null) {
			return null;
		}
		Hibernate.initialize(user.getMemories());
		entityManager.detach(user);
		user.setPassword(null);
		return user;
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

	private boolean checkPassword(String password, String passwordConfirm, BusinessException be) {
		boolean isValid = false;
		if (!password.isBlank() && password.equals(passwordConfirm)) {
			isValid = true;
		} else if (password.isBlank()) {
			be.add("Le mot de passe ne peut pas être vide.");
		} else {
			be.add("Les mots de passe ne sont pas identiques.");
		}
		return isValid;
	}

	private boolean checkPseudoAvailable(String pseudo, BusinessException be) {
		User testUser = userRepository.findByPseudo(pseudo);
		if (testUser == null) {
			return true;
		}
		be.add("Ce pseudo est déjà utilisé.");
		return false;
	}

	private boolean checkEmailAvailable(String email, BusinessException be) {
		User testUser = userRepository.findByEmail(email);
		if (testUser == null) {
			return true;
		}
		be.add("Un compte est déjà attaché à cet email.");
		return false;
	}

	@Override
	@Transactional
	public void updatePassword(User user, String rawPassword) throws BusinessException {
		BusinessException be = new BusinessException();
		Long userId = user.getUserId();
		if (rawPassword == null || rawPassword.isBlank()) {
			be.add("Vous devez renseigner le mot de passe");
			throw be;
		}
		User managedUser = userRepository.findByUserId(userId);
		String hashedPassword = passwordEncoder.encode(rawPassword);
		managedUser.setPassword(hashedPassword);

	}

}
