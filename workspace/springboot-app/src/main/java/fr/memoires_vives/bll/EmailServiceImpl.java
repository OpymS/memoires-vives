package fr.memoires_vives.bll;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Value("${app.mail.from}")
	private String fromAddress;

	public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
	}

	@Override
	public void sendPasswordResetEmail(User user, String resetLink) {
		String to = user.getEmail();
		Context context = new Context();
		context.setVariable("resetLink", resetLink);
		context.setVariable("name", user.getPseudo());

		String htmlContent = templateEngine.process("email/reset-password", context);

		sendHtmlEmail(to, "Réinitialisation de votre mot de passe", htmlContent);
	}

	private void sendHtmlEmail(String to, String subject, String htmlBody) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
			helper.setFrom(fromAddress);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlBody, true);
			mailSender.send(message);
		} catch (MessagingException | MailException e) {
			throw new EmailSendingException("Impossible d'envoyer l'email à " + to, e);
		}
	}
}
