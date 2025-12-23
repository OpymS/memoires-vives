package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EmailSendingException;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;

class EmailServiceImplTest {

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private TemplateEngine templateEngine;

	@InjectMocks
	private EmailServiceImpl emailService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		MimeMessage mimeMessage = new MimeMessage((Session) null);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

		setFromAddress(emailService, "noreply@test.com");
	}

	private void setFromAddress(EmailServiceImpl service, String value) {
		try {
			var field = EmailServiceImpl.class.getDeclaredField("fromAddress");
			field.setAccessible(true);
			field.set(service, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

//  Tests de sendPasswordResetEmail

	@Test
	void sendPasswordResetEmail_shouldGenerateTemplateAndSendEmail() {
		User user = mock(User.class);
		when(user.getEmail()).thenReturn("user@test.com");
		when(user.getPseudo()).thenReturn("Jean");

		when(templateEngine.process(eq("email/reset-password"), any(Context.class))).thenReturn("<html>reset</html>");

		emailService.sendPasswordResetEmail(user, "http://reset-link");

		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq("email/reset-password"), contextCaptor.capture());

		Context context = contextCaptor.getValue();
		assertEquals("http://reset-link", context.getVariable("resetLink"));
		assertEquals("Jean", context.getVariable("name"));
		assertNotNull(context.getVariable("date"));
		assertNotNull(context.getVariable("heure"));

		verify(mailSender).send(any(MimeMessage.class));
	}

	@SuppressWarnings("serial")
	@Test
	void sendPasswordResetEmail_shouldThrowEmailSendingException_whenMailSenderFails() {
		User user = mock(User.class);
		when(user.getEmail()).thenReturn("fail@test.com");
		when(user.getPseudo()).thenReturn("Fail");

		when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

		doThrow(new MailException("SMTP error") {
		}).when(mailSender).send(any(MimeMessage.class));

		EmailSendingException exception = assertThrows(EmailSendingException.class,
				() -> emailService.sendPasswordResetEmail(user, "link"));

		assertTrue(exception.getMessage().contains("Impossible d'envoyer l'email"));
	}

//  Tests de sendActivationEmail

	@Test
	void sendActivationEmail_shouldGenerateTemplateAndSendEmail() {
		User user = mock(User.class);
		when(user.getEmail()).thenReturn("user@test.com");
		when(user.getPseudo()).thenReturn("Marie");

		when(templateEngine.process(eq("email/activation"), any(Context.class))).thenReturn("<html>activation</html>");

		emailService.sendActivationEmail(user, "http://activation-link");

		ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
		verify(templateEngine).process(eq("email/activation"), contextCaptor.capture());

		Context context = contextCaptor.getValue();
		assertEquals("http://activation-link", context.getVariable("resetLink"));
		assertEquals("Marie", context.getVariable("name"));
		assertNotNull(context.getVariable("date"));
		assertNotNull(context.getVariable("heure"));

		verify(mailSender).send(any(MimeMessage.class));
	}

}
