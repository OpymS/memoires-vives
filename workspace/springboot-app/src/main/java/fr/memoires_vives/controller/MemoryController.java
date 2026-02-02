package fr.memoires_vives.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bll.MemoryUrlService;
import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryVisibility;
import fr.memoires_vives.dto.MemoryForm;
import fr.memoires_vives.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	private final MemoryUrlService memoryUrlService;

	public MemoryController(MemoryService memoryService, CategoryService categoryService, MemoryUrlService memoryUrlService) {
		this.memoryService = memoryService;
		this.categoryService = categoryService;
		this.memoryUrlService = memoryUrlService;
	}

	@ModelAttribute("visibilities")
	public MemoryVisibility[] visibilities() {
		return MemoryVisibility.values();
	}

	@ModelAttribute("categories")
	public List<Category> categories() {
		return categoryService.getAllCategories();
	}

	@GetMapping("/new")
	public String newMemory(Model model) {
		model.addAttribute("memoryForm", new MemoryForm());
		return "memory-form";
	}

	@PostMapping("/new")
	public String createMemory(@Valid @ModelAttribute("memoryForm") MemoryForm memoryForm, BindingResult bindingResult,
			@RequestParam(name = "image", required = false) MultipartFile fileImage) {
		if (bindingResult.hasErrors()) {
			return "memory-form";
		}

		try {
			Memory memory = memoryService.createMemory(memoryForm, fileImage);
			return "redirect:/memory/" + memory.getMemoryId() + "-" + memory.getSlug();
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

	@GetMapping({
		"/{id:\\d+}",
		"/{id:\\d+}-{slug}",
		"/{countrySlug}/{id:\\d+}-{slug}",
		"/{countrySlug}/{id:\\d+}",
		"/{countrySlug}/{citySlug}/{id:\\d+}-{slug}",
		"/{countrySlug}/{citySlug}/{id:\\d+}"
	})
	public String showMemory(@PathVariable(value = "id") Long memoryId,
			@PathVariable(value = "slug", required = false) String slug,
			@PathVariable(value = "countrySlug", required = false) String countrySlug,
			@PathVariable(value = "citySlug", required = false) String citySlug,
			HttpServletRequest request, HttpServletResponse response, Model model) {
		
		Memory memoryToDisplay = memoryService.getMemoryById(memoryId);
		
		String canonical = memoryUrlService.buildCanonicalUrl(memoryToDisplay);
		String requestedPath = request.getRequestURI();
		
		if (!requestedPath.equals(canonical)) {
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			response.setHeader("Location", canonical);
			return null;
		}

		model.addAttribute("memoryToDisplay", memoryToDisplay);
		model.addAttribute("canonicalUrl", canonical);
		return "memory";
	}

	@GetMapping("/modify")
	public String showModifyMemoryForm(@RequestParam(name = "memoryId", required = true) Long memoryId, Model model) {
		Memory memory = memoryService.getMemoryForModification(memoryId);
		MemoryForm form = MemoryForm.fromMemoryEntity(memory);
		model.addAttribute("memoryForm", form);
		return "memory-form";
	}

	@PostMapping("/modify")
	public String modifyMemory(@Valid @ModelAttribute("memoryForm") MemoryForm form, BindingResult bindingResult,
			@RequestParam(name = "image", required = false) MultipartFile fileImage,
			@RequestParam(name = "removeImage", defaultValue = "false") boolean removeImage) {

		if (bindingResult.hasErrors()) {
			return "memory-form";
		}

		try {
			Memory memory = memoryService.updateMemory(form, fileImage, removeImage);
			return "redirect:/memory/" + memory.getMemoryId() + "-" + memory.getSlug();
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
