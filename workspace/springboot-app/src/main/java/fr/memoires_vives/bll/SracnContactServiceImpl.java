package fr.memoires_vives.bll;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import fr.memoires_vives.dto.SracnContactForm;
import fr.memoires_vives.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SracnContactServiceImpl implements SracnContactService {

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Value("${app.mail.from}")
	private String fromAddress;

	public SracnContactServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
	}

	@Override
	public void send(SracnContactForm form) {
		Context context = new Context();

		context.setVariable("nom", form.getNom());
		context.setVariable("prenom", form.getPrenom());
		context.setVariable("email", form.getEmail());
		context.setVariable("telephone", form.getTelephone());
		context.setVariable("subject", form.getSubject().getLabel());
		context.setVariable("message", form.getMessage());

		String internalHtml = templateEngine.process("email/sracn/contact", context);

		sendHtmlEmail("sraclubnatation@gmail.com", "[SRACN] Nouveau message : " + form.getSubject().getLabel(),
				internalHtml, form.getEmail());

		String confirmationHtml = templateEngine.process("email/sracn/contact-confirmation", context);

		sendHtmlEmail(form.getEmail(), "SRACN - confirmation de réception", confirmationHtml,
				"sraclubnatation@gmail.com");
	}

	private void sendHtmlEmail(String to, String subject, String htmlBody, @Nullable String replyTo) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
			helper.setFrom(fromAddress);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlBody, true);

			if (replyTo != null && !replyTo.isBlank()) {
				helper.setReplyTo(replyTo);
			}

			mailSender.send(message);
		} catch (MessagingException | MailException e) {
			throw new EmailSendingException("Impossible d'envoyer l'email à " + to, e);
		}

	}
}
