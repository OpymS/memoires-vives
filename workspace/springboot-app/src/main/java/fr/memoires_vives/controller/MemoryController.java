package fr.memoires_vives.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryVisibility;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MemoryController {
	
	private static final int PAGE_SIZE = 6;

	private final MemoryService memoryService;
	private final CategoryService categoryService;

	public MemoryController(MemoryService memoryService, CategoryService categoryService) {
		this.memoryService = memoryService;
		this.categoryService = categoryService;
	}

	@GetMapping("/")
	public String home(Model model, @RequestParam(name = "currentPage", defaultValue = "1") int currentPage) {
		Page<Memory> memories = memoryService.findMemories(PageRequest.of(currentPage-1, PAGE_SIZE));
		model.addAttribute("memories", memories);
		model.addAttribute("categories", categoryService.getAllCategories());
		return "index";
	}

	@GetMapping("/about")
	public String showAboutPage() {
		return "about";
	}

	@GetMapping("/new")
	public String newMemory(Model model) {
		Memory memory = new Memory();
		Location location = new Location();
		model.addAttribute("visibilities", MemoryVisibility.values());
		model.addAttribute("memory", memory);
		model.addAttribute("location", location);
		model.addAttribute("categories", categoryService.getAllCategories());
		return "memory-form";
	}

	@PostMapping("/new")
	public String createMemory(@ModelAttribute("memory") Memory memory, @ModelAttribute("location") Location location,
			@RequestParam(name = "publish", required = false) Boolean published,
			@RequestParam(name = "image", required = false) MultipartFile fileImage) {
		memoryService.createMemory(memory, fileImage, published, location);

		return "redirect:/";
	}

	@GetMapping("/memory")
	public String showMemoryPage(@RequestParam(name = "memoryId", required = false) Long memoryId, Model model) {
		Memory memoryToDisplay = new Memory();
		if (memoryId != null && memoryId != 0) {
			memoryToDisplay = memoryService.getMemoryById(memoryId);
		} else {
			return "redirect:/";
		}
		boolean isAllowed = memoryService.authorizedDisplay(memoryToDisplay);
		if (!isAllowed) {
			return "error/403";
		}
		model.addAttribute("memoryToDisplay", memoryToDisplay);
		return "memory";
	}

	@GetMapping("/memory/modify")
	public String showModifyMemoryForm(@RequestParam(name = "memoryId", required = true) Long memoryId, Model model) {
		Memory memory = memoryService.getMemoryById(memoryId);
		boolean isAllowed = memoryService.authorizedModification(memory);
		if (!isAllowed) {
			return "error/403";
		}
		Location location = memory.getLocation();
		System.out.println(location);
		model.addAttribute("memory", memory);
		model.addAttribute("location", location);
		model.addAttribute("visibilities", MemoryVisibility.values());
		model.addAttribute("categories", categoryService.getAllCategories());
		return "memory-form";
	}

	@PostMapping("/memory/modify")
	public String modifyMemory(@ModelAttribute("memory") Memory memory, @ModelAttribute("location") Location location,
			@RequestParam(name = "publish", required = false) Boolean published,
			@RequestParam(name = "image", required = false) MultipartFile fileImage) {
		boolean isAllowed = memoryService.authorizedModification(memory);
		if (!isAllowed) {
			return "error/403";
		}
		System.out.println(location);
		memoryService.updateMemory(memory, fileImage, published, location);

		return "redirect:/memory?memoryId=" + memory.getMemoryId();
	}

}
