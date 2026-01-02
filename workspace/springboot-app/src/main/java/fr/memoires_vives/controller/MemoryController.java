package fr.memoires_vives.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryVisibility;
import fr.memoires_vives.exception.ValidationException;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/memory")
public class MemoryController {

	private final MemoryService memoryService;
	private final CategoryService categoryService;

	public MemoryController(MemoryService memoryService, CategoryService categoryService) {
		this.memoryService = memoryService;
		this.categoryService = categoryService;
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
	public String createMemory(@Valid @ModelAttribute("memory") Memory memory, BindingResult bindingResult,
			@Valid @ModelAttribute("location") Location location, BindingResult locationBindingResult,
			@RequestParam(name = "publish", required = false) Boolean published,
			@RequestParam(name = "image", required = false) MultipartFile fileImage) {
		if (bindingResult.hasErrors() || locationBindingResult.hasErrors()) {
			return "memory-form";
		}

		try {
			memoryService.createMemory(memory, fileImage, published, location);
			return "redirect:/";
		} catch (ValidationException ve) {
			ve.getGlobalErrors().forEach(err -> {
				ObjectError error = new ObjectError("globalError", err);
				bindingResult.addError(error);
			});
			ve.getFieldErrors().forEach(err -> {
				ObjectError error = new ObjectError(err.getField(), err.getMessage());
				bindingResult.addError(error);
			});
			return "memory-form";
		}

	}

	@GetMapping
	public String showMemoryPage(@RequestParam(name = "memoryId", required = false) Long memoryId, Model model) {
		Memory memoryToDisplay = memoryService.getMemoryById(memoryId);
		model.addAttribute("memoryToDisplay", memoryToDisplay);
		return "memory";
	}

	@GetMapping("/modify")
	public String showModifyMemoryForm(@RequestParam(name = "memoryId", required = true) Long memoryId, Model model) {
		Memory memory = memoryService.getMemoryForModification(memoryId);
		
		Location location = memory.getLocation();
		model.addAttribute("memory", memory);
		model.addAttribute("location", location);
		model.addAttribute("visibilities", MemoryVisibility.values());
		model.addAttribute("categories", categoryService.getAllCategories());
		return "memory-form";
	}

	@PostMapping("/modify")
	public String modifyMemory(@Valid @ModelAttribute("memory") Memory memory, BindingResult bindingResult,
			@Valid @ModelAttribute("location") Location location, BindingResult locationBindingResult,
			@RequestParam(name = "publish", required = false) Boolean published,
			@RequestParam(name = "image", required = false) MultipartFile fileImage) {
		
		if (bindingResult.hasErrors() || locationBindingResult.hasErrors()) {
			return "memory-form";
		}

		try {
			memoryService.updateMemory(memory, fileImage, published, location);
			return "redirect:/memory?memoryId=" + memory.getMemoryId();
		} catch (ValidationException ve) {
			ve.getGlobalErrors().forEach(err -> {
				ObjectError error = new ObjectError("globalError", err);
				bindingResult.addError(error);
			});
			ve.getFieldErrors().forEach(err -> {
				ObjectError error = new ObjectError(err.getField(), err.getMessage());
				bindingResult.addError(error);
			});
			return "memory-form";
		}

	}
}
