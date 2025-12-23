package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.memoires_vives.bo.Token;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EmailSendingException;
import fr.memoires_vives.exception.InvalidTokenException;
import fr.memoires_vives.repositories.TokenRepository;

@ExtendWith(MockitoExtension.class)
class ActivationServiceImplTest {

	@Mock
	private TokenService tokenService;

	@Mock
	private EmailService emailService;

	@Mock
	private TokenRepository tokenRepository;

	@InjectMocks
	private ActivationServiceImpl activationService;

	private User user;

	@BeforeEach
	void setUp() {
		user = mock(User.class);
		ReflectionTestUtils.setField(activationService, "baseUrl", "http://localhost:8080");
	}

//  Tests de requestActivation

	@Test
	void requestActivation_shouldCreateTokenAndSendEmail() {
		String token = "token123";
		when(tokenService.createTokenForUser(user, 24 * 60)).thenReturn(token);

		activationService.requestActivation(user);

		verify(tokenService).createTokenForUser(user, 24 * 60);
		verify(emailService).sendActivationEmail(eq(user),
				eq("http://localhost:8080/profil/activation?token=" + token));
	}

	@Test
	void requestActivation_shouldWrapException_whenEmailSendingFails() {
		RuntimeException rootCause = new RuntimeException("SMTP error");
		when(tokenService.createTokenForUser(any(), anyInt())).thenReturn("token123");
		doThrow(new EmailSendingException("SMTP error", rootCause)).when(emailService).sendActivationEmail(any(),
				anyString());

		EmailSendingException exception = assertThrows(EmailSendingException.class,
				() -> activationService.requestActivation(user));

		assertEquals("Un problème est survenu lors de l'envoi du mail, veuillez réessayer plus tard.",
				exception.getMessage());
		assertNotNull(exception.getCause());
	}

//  Tests de activateUser

	@Test
	void activateUser_shouldActivateUserAndDeleteToken_whenTokenIsValid() {
		String tokenValue = "validToken";
		Token token = mock(Token.class);

		when(token.getExpiration()).thenReturn(LocalDateTime.now().plusMinutes(10));
		when(token.getUser()).thenReturn(user);
		when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

		User result = activationService.activateUser(tokenValue);

		assertEquals(user, result);
		verify(user).setActivated(true);
		verify(tokenService).deleteToken(tokenValue);
	}

	@Test
    void activateUser_shouldThrowException_whenTokenDoesNotExist() {
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> activationService.activateUser("unknownToken")
        );

        assertEquals("Lien expiré ou invalide.", exception.getMessage());
        verify(tokenService, never()).deleteToken(anyString());
    }

	@Test
	void activateUser_shouldThrowException_whenTokenIsExpired() {
		Token token = mock(Token.class);

		when(token.getExpiration()).thenReturn(LocalDateTime.now().minusMinutes(1));
		when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

		InvalidTokenException exception = assertThrows(InvalidTokenException.class,
				() -> activationService.activateUser("expiredToken"));

		assertEquals("Lien expiré ou invalide.", exception.getMessage());
		verify(tokenService, never()).deleteToken(anyString());
	}

//  Tests de regenerateTokenAndSendMail

	@Test
	void regenerateTokenAndSendMail_shouldDeleteExistingTokensAndRequestActivation() {
		doNothing().when(tokenRepository).deleteAllByUser(user);
		when(tokenService.createTokenForUser(any(), anyInt())).thenReturn("newToken");

		activationService.regenerateTokenAndSendMail(user);

		verify(tokenRepository).deleteAllByUser(user);
		verify(tokenService).createTokenForUser(user, 24 * 60);
		verify(emailService).sendActivationEmail(eq(user), contains("token=newToken"));
	}
}