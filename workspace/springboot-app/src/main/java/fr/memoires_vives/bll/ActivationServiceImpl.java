package fr.memoires_vives.bll;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bo.Token;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EmailSendingException;
import fr.memoires_vives.exception.InvalidTokenException;
import fr.memoires_vives.repositories.TokenRepository;

@Service
public class ActivationServiceImpl implements ActivationService {

	private final TokenService tokenService;
	private final EmailService emailService;
	private final TokenRepository tokenRepository;

	@Value("${app.base-url}")
	private String baseUrl;

	public ActivationServiceImpl(TokenService tokenService, EmailService emailService,
			TokenRepository tokenRepository) {
		this.tokenService = tokenService;
		this.emailService = emailService;
		this.tokenRepository = tokenRepository;
	}

	@Override
	public void requestActivation(User user) {
		String token = tokenService.createTokenForUser(user, 24 * 60);
		String resetUrl = baseUrl + "/profil/activation?token=" + token;
		try {
			emailService.sendActivationEmail(user, resetUrl);
		} catch (EmailSendingException e) {
			throw new EmailSendingException(
					"Un problème est survenu lors de l'envoi du mail, veuillez réessayer plus tard.", e);
		}
	}

	@Override
	@Transactional
	public User activateUser(String token) {
		Optional<User> userOpt = tokenRepository.findByToken(token)
				.filter(activationToken -> activationToken.getExpiration().isAfter(LocalDateTime.now()))
				.map(Token::getUser);
		if (userOpt.isEmpty()) {
			throw new InvalidTokenException("Lien expiré ou invalide.");
		}
		User user = userOpt.get();
		user.setActivated(true);
		tokenService.deleteToken(token);
		return user;
	}

	@Override
	public void regenerateTokenAndSendMail(User user) {
		tokenRepository.deleteAllByUser(user);
		requestActivation(user);
	}

}
