package fr.memoires_vives.bll;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.Role;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String pseudo) throws UsernameNotFoundException {
        User user = userRepository.findByPseudo(pseudo);

        if (user == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPseudo())
                .password(user.getPassword())
                .roles(user.getRole().name().replace("ROLE_", ""))
                .build();
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
				user.setRole(Role.ROLE_USER);
				userRepository.save(user);
		} else {
			
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
}
