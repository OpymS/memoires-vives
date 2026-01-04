package fr.memoires_vives.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bll.ActivationService;
import fr.memoires_vives.bll.InvisibleCaptchaService;
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EmailSendingException;
import fr.memoires_vives.exception.ValidationException;
import fr.memoires_vives.utils.ValidationUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class LoginController {

	private final UserService userService;
	private final InvisibleCaptchaService captchaService;
	private final ActivationService activationService;

	public LoginController(UserService userService, InvisibleCaptchaService captchaService,
			ActivationService activationService) {
		this.userService = userService;
		this.captchaService = captchaService;
		this.activationService = activationService;
	}

	@GetMapping("/login")
	public String login(Model model, HttpSession session) {
		Object error = session.getAttribute("error");
		if (error != null) {
			model.addAttribute("error", error);
			session.removeAttribute("error");
		}

		Object loginUser = session.getAttribute("loginUser");
		if (loginUser != null) {
			model.addAttribute("loginUser", loginUser);
			session.removeAttribute("loginUser");
		}

		Object resend = session.getAttribute("resendActivation");
		if (resend != null) {
			model.addAttribute("resendActivation", true);
			session.removeAttribute("resendActivation");
		}
		return "login";
	}

	@GetMapping("/signup")
	public String showSignupPage(Model model) {
		User user = new User();
		model.addAttribute("user", user);
		return "signup";
	}

	@PostMapping("/signup")
	public String processSignup(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model,
			@RequestParam(name = "image", required = false) MultipartFile fileImage,
			@RequestParam(name = "website", required = false) String website,
			@RequestParam(name = "formTimestamp", required = false) Long formTimestamp,
			@RequestParam(name = "accept-conditions", required = false) Boolean acceptConditions) {

		if (captchaService.isBot(website, formTimestamp)) {
			bindingResult.addError(new ObjectError("globalError", "Requête invalide, veuillez réessayer."));
			return "signup";
		}

		if (acceptConditions == null || !acceptConditions) {
			bindingResult.addError(new ObjectError("globalError", "Veuillez accepter les CGU."));
		}
		if (bindingResult.hasErrors()) {
			return "signup";
		}
		User createdUser;
		try {
			createdUser = userService.createAccount(user.getPseudo(), user.getEmail(), user.getPassword(),
					user.getPasswordConfirm(), fileImage);
		} catch (ValidationException ve) {
			ValidationUtils.addValidationErrors(ve, bindingResult);
			return "signup";
		}
		try {
			activationService.requestActivation(createdUser);
		} catch (EmailSendingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "redirect:/login";
	}

}
