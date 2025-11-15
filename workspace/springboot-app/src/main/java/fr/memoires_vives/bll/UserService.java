package fr.memoires_vives.bll;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.User;

public interface UserService {
	public User getUserByPseudo(String pseudo);
	public User getUserByEmail(String email);
	public User getUserById(long userId);
	public User getCurrentUser();
	public User createAccount(String pseudo, String email, String password, String passwordConfirm, MultipartFile fileImage);
	public User updateProfile(User userWithUpdate, String currentPassword, MultipartFile fileImage);
	public List<User> getAllUsers();
	public boolean isAdmin();
	public boolean verifyPassword(String rawPassword);
	public void updatePassword(User user, String rawPassword);
}
