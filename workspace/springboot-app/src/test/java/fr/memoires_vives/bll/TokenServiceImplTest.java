package fr.memoires_vives.bll;

import fr.memoires_vives.bo.Token;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {

	@Mock
	private TokenRepository tokenRepository;

	@InjectMocks
	private TokenServiceImpl tokenService;

	@Mock
	private User mockUser;

	@Test
	void deleteToken_callsRepositoryWithCorrectToken() {
		String token = "abc123";

		tokenService.deleteToken(token);

		verify(tokenRepository, times(1)).deleteByToken(token);
	}

	@Test
	void createTokenForUser_savesTokenAndReturnsIt() {
		int minutes = 15;

		ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);

		String generatedToken = tokenService.createTokenForUser(mockUser, minutes);

		assertNotNull(generatedToken);

		verify(tokenRepository, times(1)).save(tokenCaptor.capture());

		Token savedToken = tokenCaptor.getValue();
		assertEquals(generatedToken, savedToken.getToken());
		assertEquals(mockUser, savedToken.getUser());

		assertTrue(savedToken.getExpiration().isAfter(LocalDateTime.now()));
	}
}
