package fr.memoires_vives.bll;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EmailSendingException;

@Service
public class ActivationServiceImpl implements ActivationService {
	
	private final TokenService tokenService;
	private final EmailService emailService;
	
	@Value("${app.base-url}")
	private String baseUrl;
	
	public ActivationServiceImpl(TokenService tokenService, EmailService emailService) {
		this.tokenService = tokenService;
		this.emailService = emailService;
	}

	@Override
	@Transactional
	public void requestActivation(User user) {
		String token = UUID.randomUUID().toString();
		tokenService.createTokenForUser(user, token, 24 * 60);
		String resetUrl = baseUrl + "/forgot-password/reset-password?token=" + token;
		try {
			emailService.sendPasswordResetEmail(user, resetUrl);
		} catch (EmailSendingException e) {
			throw new EmailSendingException(
					"Un problème est survenu lors de l'envoi du mail, veuillez réessayer plus tard.", e);
		}
	}

}
