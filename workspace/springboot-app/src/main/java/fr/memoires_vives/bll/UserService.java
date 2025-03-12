package fr.memoires_vives.bll;

import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.User;

public interface UserService {
	public void createAccount(String pseudo, String email, String password, String passwordConfirm, MultipartFile fileImage);
	public User getUserByPseudo(String pseudo);
	public User getUserById(long userId);
	public User getCurrentUser();
	public void updateProfile(User userWithUpdate, String currentPassword, MultipartFile fileImage);

}
