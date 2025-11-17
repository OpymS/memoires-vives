package fr.memoires_vives.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import fr.memoires_vives.bll.InvisibleCaptchaService;
import fr.memoires_vives.bll.ResetPasswordService;
import fr.memoires_vives.exception.EmailSendingException;
import fr.memoires_vives.exception.InvalidTokenException;
import fr.memoires_vives.exception.ValidationException;

@Controller
@RequestMapping("/forgot-password")
public class PasswordResetController {

	private final ResetPasswordService resetPasswordService;
	private final InvisibleCaptchaService captchaService;

	@Value("${app.base-url}")
	private String baseUrl;

	public PasswordResetController(ResetPasswordService resetPasswordService, InvisibleCaptchaService captchaService) {
		this.resetPasswordService = resetPasswordService;
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

		if (captchaService.isBot(website, formTimestamp)) {
			model.addAttribute("errorMessage", "Requête invalide, veuillez réessayer.");
			return "forgot-password";
		}

		try {
			resetPasswordService.requestPasswordReset(email);
		} catch (EmailSendingException e) {
			model.addAttribute("errorMessage",
					"Un problème est survenu lors de l'envoi du mail, veuillez réessayer plus tard.");
		}
		model.addAttribute("message", "Si l'adresse existe, vous recevrez un mail avec un lien de réinitialisation");
		return "forgot-password";
	}

	@GetMapping("/reset-password")
	public String displayResetPasswordPage(@RequestParam("token") String token, Model model) {
		try {
			resetPasswordService.validatePasswordResetToken(token);
		} catch (InvalidTokenException e) {
			model.addAttribute("message", e.getMessage());
			return "reset-password-error";
		}
		model.addAttribute("token", token);
		return "reset-password";
	}

	@PostMapping("/reset-password")
	public String handleResetPassword(@RequestParam("token") String token, @RequestParam("password") String password,
			Model model) {

		try {
			resetPasswordService.resetPassword(token, password);
			return "reset-password-success";
		} catch (InvalidTokenException e) {
			model.addAttribute("message", e.getMessage());
			return "reset-password-error";
		} catch (ValidationException ve) {
			model.addAttribute("message", ve.getMessage());
			return "reset-password-error";
		}

	}

}
