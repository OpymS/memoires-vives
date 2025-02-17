package fr.memoires_vives.bll;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import fr.memoires_vives.bo.User;

public interface UserService extends UserDetailsService {
	public UserDetails loadUserByUsername(String pseudo) throws UsernameNotFoundException;
	public void createAccount(String pseudo, String email, String password, String passwordConfirm);
	public User getUserByPseudo(String pseudo);

}
