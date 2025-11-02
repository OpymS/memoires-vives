package fr.memoires_vives.bll;

import java.util.Optional;

import fr.memoires_vives.bo.User;

public interface PasswordResetTokenService {
	public void createPasswordResetTokenForUser(User user, String token);
	public Optional<User> validatePasswordResetToken(String token);
	public void deleteToken(String token);
}
