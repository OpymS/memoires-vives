package fr.memoires_vives.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.Memory;

@Controller
public class HomeController {
	private static final int PAGE_SIZE = 6;
	
	private final MemoryService memoryService;
	private final CategoryService categoryService;
	
	public HomeController(MemoryService memoryService, CategoryService categoryService) {
		this.memoryService = memoryService;
		this.categoryService = categoryService;
		
	}

	@GetMapping("/")
	public String home(Model model, @RequestParam(name = "currentPage", defaultValue = "1") int currentPage) {
		Page<Memory> memories = memoryService.findMemories(PageRequest.of(currentPage - 1, PAGE_SIZE));
		model.addAttribute("memories", memories);
		model.addAttribute("categories", categoryService.getAllCategories());
		return "index";
	}

}
