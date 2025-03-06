package fr.memoires_vives.bll;

import org.hibernate.Hibernate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bo.Role;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.RoleRepository;
import fr.memoires_vives.repositories.UserRepository;
import fr.memoires_vives.security.CustomUserDetails;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
			RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void createAccount(String pseudo, String email, String password, String passwordConfirm) {
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

			userRepository.save(user);
		} else {
			throw new IllegalArgumentException("Les informations fournies ne sont pas valides.");
		}
	}

	private boolean checkPassword(String password, String passwordConfirm) {
		return true;
	}

	private boolean checkPseudoAvailable(String pseudo) {
		return true;
	}

	private boolean checkEmailAvailable(String email) {
		return true;
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
		return user;
	}

	@Override
	@Transactional(readOnly = true)
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		User user = userRepository.findByUserId(userDetails.getUser().getUserId());
//		user.setPassword(null);
		return user;
	}
}
