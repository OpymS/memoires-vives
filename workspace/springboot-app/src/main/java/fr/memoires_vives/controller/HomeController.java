package fr.memoires_vives.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bll.MemoryUrlService;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.dto.MemoryView;

@Controller
public class HomeController {
	
	private static final int PAGE_SIZE = 12;
	
	private final CategoryService categoryService;
	private final MemoryService memoryService;
	private final MemoryUrlService memoryUrlService;
	
	public HomeController(CategoryService categoryService, MemoryService memoryService, MemoryUrlService memoryUrlService) {
		this.categoryService = categoryService;
		this.memoryService = memoryService;
		this.memoryUrlService = memoryUrlService;
		
	}

	@GetMapping("/")
	public String home(Model model) {
		Page<Memory> memories = memoryService.findMemoriesWithCriteria(PageRequest.of(0, PAGE_SIZE), null);
		Page<MemoryView> views = memories.map(m -> new MemoryView(m, memoryUrlService.buildCanonicalUrl(m)));
		model.addAttribute("views", views);
		model.addAttribute("categories", categoryService.getAllCategories());
		return "index";
	}

}
