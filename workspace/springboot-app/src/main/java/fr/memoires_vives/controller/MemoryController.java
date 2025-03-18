package fr.memoires_vives.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
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

@Controller
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
		return "memory-create";
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
		model.addAttribute("memory", memory);
		model.addAttribute("location", location);
		model.addAttribute("visibilities", MemoryVisibility.values());
		return "memory-modify";
	}

	@PostMapping("/memory/modify")
	public String modifyMemory(@ModelAttribute("memory") Memory memory, @ModelAttribute("location") Location location,
			@RequestParam(name = "publish", required = false) Boolean published,
			@RequestParam(name = "image", required = false) MultipartFile fileImage) {
		boolean isAllowed = memoryService.authorizedModification(memory);
		if (!isAllowed) {
			return "error/403";
		}
		memoryService.updateMemory(memory, fileImage, published, location);
		
		return "redirect:/memory?memoryId="+memory.getMemoryId();
	}

}
