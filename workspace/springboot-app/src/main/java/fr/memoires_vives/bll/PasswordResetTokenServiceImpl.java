package fr.memoires_vives.bll;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.PasswordResetToken;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EmailSendingException;
import fr.memoires_vives.exception.InvalidTokenException;
import fr.memoires_vives.repositories.PasswordResetTokenRepository;
import fr.memoires_vives.repositories.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

	private final PasswordResetTokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;
	private final UserService userService;

	@Value("${app.base-url}")
	private String baseUrl;

	public PasswordResetTokenServiceImpl(PasswordResetTokenRepository tokenRepository, UserRepository userRepository,
			EmailService emailService, UserService userService) {
		this.tokenRepository = tokenRepository;
		this.userRepository = userRepository;
		this.emailService = emailService;
		this.userService = userService;
	}

	@Override
	public void createPasswordResetTokenForUser(User user, String token) {
		PasswordResetToken passwordResetToken = new PasswordResetToken(token, user,
				LocalDateTime.now().plusMinutes(30));
		tokenRepository.save(passwordResetToken);
	}

	@Override
	@Transactional
	public void requestPasswordReset(String email) {
		User user = userRepository.findByEmail(email).orElse(null);

		if (user != null) {
			String token = UUID.randomUUID().toString();
			createPasswordResetTokenForUser(user, token);
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
		deleteToken(token);
	}
	
	@Override
	public User validatePasswordResetToken(String token) {
		Optional<User> user = tokenRepository.findByToken(token)
				.filter(passwordResetToken -> passwordResetToken.getExpiration().isAfter(LocalDateTime.now()))
				.map(PasswordResetToken::getUser);
		if (user.isEmpty()) {
			throw new InvalidTokenException("Lien expiré ou invalide.");
		}
		return user.get();
	}

	@Override
	@Transactional
	public void deleteToken(String token) {
		tokenRepository.deleteByToken(token);
	}

}
