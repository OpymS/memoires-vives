package fr.memoires_vives.bll;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
	public void createAccount(String pseudo, String email, String password, String passwordConfirm);

}
