package fr.memoires_vives.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fr.memoires_vives.bll.CategoryService;

@Controller
public class HomeController {
	
	private final CategoryService categoryService;
	
	public HomeController(CategoryService categoryService) {
		this.categoryService = categoryService;
		
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("categories", categoryService.getAllCategories());
		return "index";
	}

}
