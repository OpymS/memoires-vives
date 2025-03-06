package fr.memoires_vives.controller;

import java.util.List;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryVisibility;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@org.springframework.stereotype.Controller
public class MemoryController {

	private final MemoryService memoryService;

	public MemoryController(MemoryService memoryService) {
		this.memoryService = memoryService;
	}

	@GetMapping("/")
	public String home(Model model) {
		List<Memory> memories = memoryService.findMemories();
		model.addAttribute("memories", memories);
		return "index";
	}

	@GetMapping("/new")
	public String newMemory(Model model) {
		Memory memory = new Memory();
		Location location = new Location();
		model.addAttribute("visibilities", MemoryVisibility.values());
		model.addAttribute("memory", memory);
		model.addAttribute("location", location);
		return "memory-create";
	}

	@PostMapping("/new")
	public String createMemory(@ModelAttribute("memory") Memory memory,
			@ModelAttribute("location") Location location,
			@RequestParam(name = "publish", required = false) Boolean published,
			@RequestParam(name = "image", required = false) MultipartFile fileImage) {
		memoryService.createMemory(memory, fileImage, published, location);

		return "redirect:/";
	}

}
