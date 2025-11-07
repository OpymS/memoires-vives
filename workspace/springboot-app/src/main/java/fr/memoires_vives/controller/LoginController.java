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
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.BusinessException;
import jakarta.validation.Valid;

@Controller
public class LoginController {

	private final UserService userService;

	public LoginController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String login() {
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
			@RequestParam(name = "formTimestamp", required = false) Long formTimestamp) {

		if ((website != null && !website.isBlank()) || formTimestamp == null
				|| (System.currentTimeMillis() - formTimestamp < 3000)) {
			bindingResult.addError(new ObjectError("globalError", "Requête invalide, veuillez réessayer."));
			return "signup";
		}

		if (bindingResult.hasErrors()) {
			return "signup";
		}
		try {
			userService.createAccount(user.getPseudo(), user.getEmail(), user.getPassword(), user.getPasswordConfirm(),
					fileImage);
			return "redirect:/login";
		} catch (BusinessException e) {
			e.getErrors().forEach(err -> {
//				String errorMessage = messageSource.getMessage(err, null, locale);
				ObjectError error = new ObjectError("globalError", err);
				bindingResult.addError(error);
			});
			return "signup";
		}
	}

}
