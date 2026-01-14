package fr.memoires_vives.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.Memory;

@Controller
public class HomeController {
	
	private static final int PAGE_SIZE = 12;
	
	private final CategoryService categoryService;
	
	private final MemoryService memoryService;
	
	public HomeController(CategoryService categoryService, MemoryService memoryService) {
		this.categoryService = categoryService;
		this.memoryService = memoryService;
		
	}

	@GetMapping("/")
	public String home(Model model) {
		Page<Memory> memories = memoryService.findMemoriesWithCriteria(PageRequest.of(0, PAGE_SIZE), null);
		model.addAttribute("memories", memories);
		model.addAttribute("categories", categoryService.getAllCategories());
		return "index";
	}

}
