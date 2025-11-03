package fr.memoires_vives.bll;

import fr.memoires_vives.exception.BusinessException;

public interface EmailService {
	void sendEmail(String to, String token) throws BusinessException;
	void sendPasswordResetEmail(String to, String resetLink);
}
