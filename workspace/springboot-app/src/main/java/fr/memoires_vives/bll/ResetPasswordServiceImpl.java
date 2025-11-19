package fr.memoires_vives.bll;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.Token;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EmailSendingException;
import fr.memoires_vives.exception.InvalidTokenException;
import fr.memoires_vives.repositories.TokenRepository;
import fr.memoires_vives.repositories.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordServiceImpl implements ResetPasswordService {

	private final TokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;
	private final UserService userService;
	private final TokenService tokenService;

	@Value("${app.base-url}")
	private String baseUrl;

	public ResetPasswordServiceImpl(TokenRepository tokenRepository, UserRepository userRepository,
			EmailService emailService, UserService userService, TokenService tokenService) {
		this.tokenRepository = tokenRepository;
		this.userRepository = userRepository;
		this.emailService = emailService;
		this.userService = userService;
		this.tokenService = tokenService;
	}

	@Override
	@Transactional
	public void requestPasswordReset(String email) {
		User user = userRepository.findByEmail(email).orElse(null);

		if (user != null) {
			tokenRepository.deleteAllByUser(user);
			String token = tokenService.createTokenForUser(user, 30);
			String resetUrl = baseUrl + "/forgot-password/reset-password?token=" + token;
			try {
				emailService.sendPasswordResetEmail(user, resetUrl);
			} catch (EmailSendingException e) {
				throw new EmailSendingException(
						"Un problème est survenu lors de l'envoi du mail, veuillez réessayer plus tard.", e);
			}
		}
	}

	@Override
	@Transactional
	public void resetPassword(String token, String newPassword) {
		User user = validatePasswordResetToken(token);
		userService.updatePassword(user, newPassword);
		tokenService.deleteToken(token);
	}

	@Override
	public User validatePasswordResetToken(String token) {
		Optional<User> user = tokenRepository.findByToken(token)
				.filter(passwordResetToken -> passwordResetToken.getExpiration().isAfter(LocalDateTime.now()))
				.map(Token::getUser);
		if (user.isEmpty()) {
			throw new InvalidTokenException("Lien expiré ou invalide.");
		}
		return user.get();
	}

}
