package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.memoires_vives.bo.Token;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EmailSendingException;
import fr.memoires_vives.exception.InvalidTokenException;
import fr.memoires_vives.repositories.TokenRepository;
import fr.memoires_vives.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ResetPasswordServiceImplTest {

	@Mock
	private TokenRepository tokenRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private EmailService emailService;

	@Mock
	private UserService userService;

	@Mock
	private TokenService tokenService;

	@InjectMocks
	private ResetPasswordServiceImpl resetPasswordService;

	private final String baseUrl = "http://localhost:8080";

//  Tests de requestPasswordReset

	@Test
	void requestPasswordReset_noUser_doesNothing() {
		String email = "unknown@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		resetPasswordService.requestPasswordReset(email);

		verify(userRepository).findByEmail(email);
		verifyNoMoreInteractions(tokenRepository, tokenService, emailService);
	}

	@Test
	void requestPasswordReset_existingUser_sendsEmail() throws EmailSendingException {
		String email = "john@example.com";
		User user = new User();
		user.setEmail(email);
		String token = "token123";

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenService.createTokenForUser(user, 30)).thenReturn(token);

		ReflectionTestUtils.setField(resetPasswordService, "baseUrl", "http://localhost:8080");

		resetPasswordService.requestPasswordReset(email);

		verify(tokenRepository).deleteAllByUser(user);
		verify(tokenService).createTokenForUser(user, 30);
		verify(emailService).sendPasswordResetEmail(user, baseUrl + "/forgot-password/reset-password?token=" + token);
	}

	@Test
	void requestPasswordReset_emailFails_throwsEmailSendingException() throws EmailSendingException {
		String email = "john@example.com";
		User user = new User();
		user.setEmail(email);
		String token = "token123";

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenService.createTokenForUser(user, 30)).thenReturn(token);
		doThrow(new EmailSendingException("fail", new RuntimeException())).when(emailService)
				.sendPasswordResetEmail(any(User.class), anyString());

		ReflectionTestUtils.setField(resetPasswordService, "baseUrl", "http://localhost:8080");

		EmailSendingException ex = assertThrows(EmailSendingException.class,
				() -> resetPasswordService.requestPasswordReset(email));

		assertTrue(ex.getMessage().contains("Un problème est survenu"));
		verify(tokenRepository).deleteAllByUser(user);
		verify(tokenService).createTokenForUser(user, 30);
		verify(emailService).sendPasswordResetEmail(user, baseUrl + "/forgot-password/reset-password?token=" + token);
	}

//  Tests de resetPassword

	@Test
	void resetPassword_callsUpdateAndDeletesToken_whenTokenIsValid() {
		String token = "token123";
		String newPassword = "newPass";
		User mockUser = new User();
		ResetPasswordServiceImpl spyService = Mockito.spy(resetPasswordService);
		doReturn(mockUser).when(spyService).validatePasswordResetToken(token);

		spyService.resetPassword(token, newPassword);

		verify(userService, times(1)).updatePassword(mockUser, newPassword);
		verify(tokenService, times(1)).deleteToken(token);
	}

	@Test
	void resetPassword_throwsException_whenTokenIsInvalid() {
		String token = "badToken";
		String newPassword = "newPass";
		ResetPasswordServiceImpl spyService = Mockito.spy(resetPasswordService);
		doThrow(new IllegalArgumentException("Invalid token")).when(spyService).validatePasswordResetToken(token);

		assertThrows(IllegalArgumentException.class, () -> spyService.resetPassword(token, newPassword));

		verify(userService, never()).updatePassword(any(), anyString());
		verify(tokenService, never()).deleteToken(anyString());
	}

	@Test
	void resetPassword_throwsException_whenUpdatePasswordFails() {
		String token = "token123";
		String newPassword = "newPass";
		User mockUser = new User();
		ResetPasswordServiceImpl spyService = Mockito.spy(resetPasswordService);
		doReturn(mockUser).when(spyService).validatePasswordResetToken(token);
		doThrow(new RuntimeException("DB error")).when(userService).updatePassword(mockUser, newPassword);

		assertThrows(RuntimeException.class, () -> spyService.resetPassword(token, newPassword));

		verify(tokenService, never()).deleteToken(anyString());
	}

//  Tests de validatePasswordResetToken

	@Test
	void validatePasswordResetToken_returnsUser_whenTokenIsValid() {
		String token = "token123";
		User user = new User();
		Token validToken = new Token(token, user, LocalDateTime.now().plusMinutes(10));

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(validToken));

		User result = resetPasswordService.validatePasswordResetToken(token);

		assertNotNull(result);
		assertEquals(user, result);
	}

	@Test
	void validatePasswordResetToken_throwsException_whenTokenNotFound() {
		String token = "missingToken";
		when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

		assertThrows(InvalidTokenException.class, () -> resetPasswordService.validatePasswordResetToken(token));
	}

	@Test
	void validatePasswordResetToken_throwsException_whenTokenExpired() {
		String token = "expiredToken";
		User user = new User();
		Token expiredToken = new Token(token, user, LocalDateTime.now().minusMinutes(5));

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(expiredToken));

		assertThrows(InvalidTokenException.class, () -> resetPasswordService.validatePasswordResetToken(token));
	}
}
