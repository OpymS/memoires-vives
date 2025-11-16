package fr.memoires_vives.bll;

import fr.memoires_vives.bo.User;

public interface ResetPasswordService {
	public void requestPasswordReset(String email);
	public User validatePasswordResetToken(String token);
	public void resetPassword(String token, String newPassword);
}
