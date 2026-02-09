package fr.memoires_vives.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.LocationService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bll.MemoryUrlService;
import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.dto.MemoryView;
import fr.memoires_vives.dto.SearchCriteria;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/memories")
public class MemorySearchController {
	private static final int PAGE_SIZE = 12;

	private final MemoryService memoryService;
	private final CategoryService categoryService;
	private final MemoryUrlService memoryUrlService;
	private final LocationService locationService;

	public MemorySearchController(MemoryService memoryService, CategoryService categoryService,
			MemoryUrlService memoryUrlService, LocationService locationService) {
		this.memoryService = memoryService;
		this.categoryService = categoryService;
		this.memoryUrlService = memoryUrlService;
		this.locationService = locationService;
	}


	@ModelAttribute("categories")
	public List<Category> categories() {
		return categoryService.getAllCategories();
	}

	@GetMapping("/{countrySlug}")
	public String showMemoriesByCountry(@PathVariable(value = "countrySlug") String countrySlug,
			HttpServletRequest request, Model model) {
		SearchCriteria criterias = new SearchCriteria();
		criterias.setCountrySlug(countrySlug);
		model.addAttribute("countrySlug", countrySlug);

		Page<Memory> memories = memoryService.findMemoriesWithCriteria(PageRequest.of(0, PAGE_SIZE), criterias);
		Page<MemoryView> views = memories.map(m -> new MemoryView(m, memoryUrlService.buildCanonicalUrl(m)));
		views.forEach((view) -> {
			System.out.println(view.memory() + " - " + view.canonicalUrl());
		});
		model.addAttribute("views", views);

		String countryLabel = locationService.resolveCountryLabel(countrySlug).orElse(countrySlug);
		model.addAttribute("country", countryLabel);

		String url = request.getRequestURL().toString();
		model.addAttribute("canonicalUrl", url);

		return "memory-geographical";
	}

	@GetMapping("/{countrySlug}/{citySlug}")
	public String showMemoriesByCity(@PathVariable(value = "countrySlug") String countrySlug,
			@PathVariable(value = "citySlug") String citySlug, HttpServletRequest request, Model model) {
		System.out.println("citySlug : " + citySlug);
		SearchCriteria criterias = new SearchCriteria();
		criterias.setCountrySlug(countrySlug);
		criterias.setCitySlug(citySlug);
		model.addAttribute("countrySlug", countrySlug);
		model.addAttribute("citySlug", citySlug);

		Page<Memory> memories = memoryService.findMemoriesWithCriteria(PageRequest.of(0, PAGE_SIZE), criterias);
		Page<MemoryView> views = memories.map(m -> new MemoryView(m, memoryUrlService.buildCanonicalUrl(m)));
		views.forEach((view) -> {
			System.out.println(view.memory() + " - " + view.canonicalUrl());
		});
		model.addAttribute("views", views);

		String countryLabel = locationService.resolveCountryLabel(countrySlug).orElse(countrySlug);
		model.addAttribute("country", countryLabel);

		String cityLabel = locationService.resolveCityLabel(citySlug, countrySlug).orElse(citySlug);
		model.addAttribute("city", cityLabel);

		String url = request.getRequestURL().toString();
		model.addAttribute("canonicalUrl", url);

		model.addAttribute("categories", categoryService.getAllCategories());

		return "memory-geographical";
	}

}
