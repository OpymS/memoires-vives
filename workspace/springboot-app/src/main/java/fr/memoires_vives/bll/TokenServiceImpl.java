package fr.memoires_vives.bll;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bo.Token;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.TokenRepository;

@Service
public class TokenServiceImpl implements TokenService {
	
	private final TokenRepository tokenRepository;
	
	public TokenServiceImpl(TokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}

	@Override
	@Transactional
	public void deleteToken(String token) {
		tokenRepository.deleteByToken(token);
	}


	@Override
	public String createTokenForUser(User user, int minutes) {
		String token = UUID.randomUUID().toString();
		Token passwordResetToken = new Token(token, user,
				LocalDateTime.now().plusMinutes(minutes));
		tokenRepository.save(passwordResetToken);
		return token;
	}

}
