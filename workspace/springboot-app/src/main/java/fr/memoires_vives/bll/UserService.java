package fr.memoires_vives.bll;

import fr.memoires_vives.bo.User;

public interface UserService {
	public void createAccount(String pseudo, String email, String password, String passwordConfirm);
	public User getUserByPseudo(String pseudo);

}
