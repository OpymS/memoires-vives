package fr.memoires_vives.bll;

import fr.memoires_vives.bo.User;

public interface EmailService {
	
	void sendPasswordResetEmail(User user, String resetLink);
}
