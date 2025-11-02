package fr.memoires_vives.bll;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import fr.memoires_vives.exception.BusinessException;

@Service
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;
	
	@Value("${app.mail.from}")
	private String fromAddress;
	
	public EmailServiceImpl(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	@Override
	public void sendEmail(String to, String token) throws BusinessException {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(to);
		message.setSubject("test d'envoi");
		message.setText("et on va voir si ça marche");
		mailSender.send(message);
	}

}
