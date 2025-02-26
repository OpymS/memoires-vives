package fr.memoires_vives.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String pseudo) throws UsernameNotFoundException {
		User user = userRepository.findByPseudoWithFriends(pseudo);
		if (user == null) {
			throw new UsernameNotFoundException("Utilisateur non trouvé : " + pseudo);
		}
		return new CustomUserDetails(user);
	}
}
