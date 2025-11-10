package fr.memoires_vives.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import fr.memoires_vives.bll.EmailService;
import fr.memoires_vives.bll.InvisibleCaptchaService;
import fr.memoires_vives.bll.PasswordResetTokenService;
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.BusinessException;


@Controller
@RequestMapping("/forgot-password")
public class PasswordResetController {

	private final PasswordResetTokenService tokenService;
	private final UserService userService;
	private final EmailService emailService;
	private final InvisibleCaptchaService captchaService;
	
	@Value("${app.base-url}")
	private String baseUrl;

	public PasswordResetController(UserService userService, PasswordResetTokenService tokenService, EmailService emailService, InvisibleCaptchaService captchaService) {
		this.tokenService = tokenService;
		this.userService = userService;
		this.emailService = emailService;
		this.captchaService = captchaService;
	}

		
	@GetMapping
	public String forgotPasswordForm() {
		return "forgot-password";
	}
	
	@PostMapping
	public String handleForgotPassword(@RequestParam("email") String email, Model model,
			@RequestParam(name = "website", required = false) String website,
			@RequestParam(name = "formTimestamp", required = false) Long formTimestamp) {
		
		if (!captchaService.isBot(website, formTimestamp)) {
			User user = userService.getUserByEmail(email);
			if (user != null) {
				String token = UUID.randomUUID().toString();
				tokenService.createPasswordResetTokenForUser(user, token);
				String resetUrl = baseUrl + "/forgot-password/reset-password?token=" + token;
				emailService.sendPasswordResetEmail(user, resetUrl);
			} 
		}
		model.addAttribute("message", "Si l'adresse existe, vous recevrez un mail avec un lien de réinitialisation");
		return "forgot-password";
	}
	
	@GetMapping("/reset-password")
	public String displayResetPasswordPage(@RequestParam("token") String token, Model model) {
		Optional<User> user = tokenService.validatePasswordResetToken(token);
		if (user.isEmpty()) {
			model.addAttribute("message", "Token invalide ou expiré");
			return "reset-password-error";
		}
		model.addAttribute("token", token);
		return "reset-password";
	}
	
	@PostMapping("/reset-password")
	public String handleResetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {
		Optional<User> userOpt = tokenService.validatePasswordResetToken(token);
		if (userOpt.isEmpty()) {
			model.addAttribute("message", "Token invalide ou expiré");
			return "reset-password-error";
		}
		
		User user = userOpt.get();
		try {
			userService.updatePassword(user, password);
			tokenService.deleteToken(token);
			return "reset-password-success";
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "reset-password-error";
		}
		
	}
	
	
}
