package fr.memoires_vives.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.User;

@Controller
@RequestMapping("/admin")
public class AdminController {
	private final UserService userService;
	
	public AdminController(UserService userService) {
		this.userService = userService;
	}
	
	

	@GetMapping
	public String showAdminPanel() {
		return "/admin";
	}
	
	@GetMapping("/users")
	public String showAdminUsers(Model model) {
		List<User> users = userService.getAllUsers();
		model.addAttribute("users", users);
		return "admin-users";
	}
	
}
