package fr.memoires_vives.bll;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.UserRepository;

@Service
public class CustomUserDetailService implements UserDetailsServiceInterface {
	private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                .roles(user.getRole())
                .build();
    }
}
