package fr.memoires_vives.bll;

import java.io.IOException;

import org.hibernate.Hibernate;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Role;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.RoleRepository;
import fr.memoires_vives.repositories.UserRepository;
import fr.memoires_vives.security.CustomUserDetails;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final FileService fileService;

	@PersistenceContext
	private EntityManager entityManager;

	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
			RoleRepository roleRepository, FileService fileService) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.fileService = fileService;
	}

	@Override
	public User createAccount(String pseudo, String email, String password, String passwordConfirm,
			MultipartFile image) {
		boolean isValid = checkPassword(password, passwordConfirm) && checkPseudoAvailable(pseudo)
				&& checkEmailAvailable(email);

		if (isValid) {
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				user.setMediaUUID(null);
			}

			return userRepository.save(user);
		} else {
			throw new IllegalArgumentException(
					"Les informations fournies ne sont pas valides. Soit le pseudo est déjà pris, soit c'est l'email soit y a un problème de mdp");
		}
	}

	private boolean checkPassword(String password, String passwordConfirm) {
		boolean isValid = false;
		if (!password.isBlank() && password.equals(passwordConfirm)) {
			isValid = true;
		}
		return isValid;
	}

	private boolean checkPseudoAvailable(String pseudo) {
		User testUser = userRepository.findByPseudo(pseudo);
		if (testUser == null) {
			return true;
		}
		return false;
	}

	private boolean checkEmailAvailable(String email) {
		User testUser = userRepository.findByEmail(email);
		if (testUser == null) {
			return true;
		}
		return false;
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
	public User updateProfile(User userWithUpdate, String currentPassword, MultipartFile fileImage) {
		boolean isValid = true;

		User userToSave = userRepository.findByUserId(userWithUpdate.getUserId());
		String updatedPseudo = userWithUpdate.getPseudo();
		String currentPseudo = userToSave.getPseudo();

		String updatedEmail = userWithUpdate.getEmail();
		String currentEmail = userToSave.getEmail();

		isValid &= passwordEncoder.matches(currentPassword, userToSave.getPassword());

		if (!userWithUpdate.getPassword().isBlank() || !userWithUpdate.getPasswordConfirm().isBlank()) {
			isValid &= checkPassword(userWithUpdate.getPassword(), userWithUpdate.getPasswordConfirm());
			if (isValid) {
				userToSave.setPassword(passwordEncoder.encode(userWithUpdate.getPassword()));
			}
		}

		if (!updatedPseudo.equals(currentPseudo)) {
			isValid &= checkPseudoAvailable(updatedPseudo);
			userToSave.setPseudo(updatedPseudo);
		}

		if (!updatedEmail.equals(currentEmail)) {
			isValid &= checkEmailAvailable(updatedEmail);
			userToSave.setEmail(updatedEmail);
		}

		if (fileImage != null && !fileImage.isEmpty()) {
			try {
				userToSave.setMediaUUID(fileService.saveUserFile(fileImage, updatedPseudo));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (isValid) {
			try {
				return userRepository.save(userToSave);
			} catch (DataAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
