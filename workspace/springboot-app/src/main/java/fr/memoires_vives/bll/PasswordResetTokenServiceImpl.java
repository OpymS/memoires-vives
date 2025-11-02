package fr.memoires_vives.bll;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.PasswordResetToken;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.PasswordResetTokenRepository;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

	private final PasswordResetTokenRepository tokenRepository;

	public PasswordResetTokenServiceImpl(PasswordResetTokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}

	@Override
	public void createPasswordResetTokenForUser(User user, String token) {
		PasswordResetToken passwordResetToken = new PasswordResetToken(token, user,
				LocalDateTime.now().plusMinutes(30));
		tokenRepository.save(passwordResetToken);
	}

	@Override
	public Optional<User> validatePasswordResetToken(String token) {
		return tokenRepository.findByToken(token)
				.filter(passwordResetToken -> passwordResetToken.getExpiration().isAfter(LocalDateTime.now()))
				.map(PasswordResetToken::getUser);
	}

	@Override
	public void deleteToken(String token) {
		tokenRepository.deleteByToken(token);
	}

}
