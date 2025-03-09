package fr.memoires_vives.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.User;

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
	public String processSignup(@ModelAttribute("user") User user, Model model, @RequestParam(name = "image", required = false) MultipartFile fileImage) {
		userService.createAccount(user.getPseudo(), user.getEmail(), user.getPassword(), user.getPasswordConfirm(), fileImage);
		return "redirect:/login";
	}
}
