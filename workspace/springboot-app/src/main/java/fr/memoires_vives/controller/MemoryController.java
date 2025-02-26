package fr.memoires_vives.controller;

import java.util.List;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryVisibility;

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
	public String newMemory(Model model){
		Memory memory = new Memory();
		model.addAttribute("visibilities", MemoryVisibility.values());
		model.addAttribute("memory", memory);
		return "memory-create";
	}
}
