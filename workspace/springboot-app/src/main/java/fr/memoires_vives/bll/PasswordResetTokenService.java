package fr.memoires_vives.bll;

import fr.memoires_vives.bo.User;

public interface PasswordResetTokenService {
	public void createPasswordResetTokenForUser(User user, String token);
	public void requestPasswordReset(String email);
	public User validatePasswordResetToken(String token);
	public void resetPassword(String token, String newPassword);
	public void deleteToken(String token);
}
