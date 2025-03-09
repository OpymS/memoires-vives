package fr.memoires_vives.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.User;

@Controller
@RequestMapping("/profil")
public class ProfilController {
	private final UserService userService;

	public ProfilController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping
	public String showProfilPage(@RequestParam(name = "userId", required = false) Long userId, Model model) {
		User userToDisplay = new User();
		if (userId != null && userId != 0) {
			userToDisplay = userService.getUserById(userId);
		}
		else {
			userToDisplay = userService.getCurrentUser();		
		}
		userToDisplay.setPassword(null);
		model.addAttribute("userToDisplay", userToDisplay);

		return "profil";
	}
	
	@GetMapping("/modify")
	public String showModifyProfilForm(Model model) {
		User user = userService.getCurrentUser();
		model.addAttribute("user", user);
		return "profil-modify";
	}
	
	@PostMapping("/modify")
	public String postMethodName(@RequestBody String entity) {
		//TODO: process POST request
		
		return entity;
	}
	
}
