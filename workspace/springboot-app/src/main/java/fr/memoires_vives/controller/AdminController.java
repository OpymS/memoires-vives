package fr.memoires_vives.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.LocationService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bll.MemoryUrlService;
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.component.CaptchaCounter;
import fr.memoires_vives.dto.MemoryView;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.mapper.MemoryViewMapper;

@Controller
@RequestMapping("/admin")
public class AdminController {
	private static final int PAGE_SIZE = 500;

	private final UserService userService;
	private final MemoryService memoryService;
	private final CategoryService categoryService;
	private final LocationService locationService;
	private final CaptchaCounter counter;
	private final MemoryViewMapper memoryViewMapper;

	public AdminController(UserService userService, MemoryService memoryService, CategoryService categoryService,
			LocationService locationService, CaptchaCounter counter, MemoryUrlService memoryUrlService, MemoryViewMapper memoryViewMapper) {
		this.userService = userService;
		this.memoryService = memoryService;
		this.categoryService = categoryService;
		this.locationService = locationService;
		this.counter = counter;
		this.memoryViewMapper = memoryViewMapper;
	}

	@GetMapping
	public String showAdminPanel(Model model) {
		model.addAttribute("counter", counter.getCount());
		return "admin";
	}

	@GetMapping("/users")
	public String showAdminUsers(Model model) {
		List<User> users = userService.getAllUsers();
		model.addAttribute("users", users);
		return "admin-users";
	}

	@GetMapping("/memories")
	public String showAdminMemories(Model model,
			@RequestParam(name = "currentPage", defaultValue = "1") int currentPage) {
		Page<Memory> memories = memoryService.findMemories(PageRequest.of(currentPage - 1, PAGE_SIZE));
		Page<MemoryView> views = memories.map(memoryViewMapper::toView);
		model.addAttribute("views", views);
		return "admin-memories";
	}

	@GetMapping("/categories")
	public String showAdminCategories(Model model) {
		List<Category> categories = categoryService.getAllCategories();
		model.addAttribute("categories", categories);
		return "admin-categories";
	}

	@GetMapping("/locations")
	public String showAdminLocations(Model model) {
		List<Location> locations = locationService.getAllLocations();
		model.addAttribute("locations", locations);
		return "admin-locations";
	}

	@GetMapping("/category/new")
	public String showCategoryForm(Model model) {
		Category category = new Category();
		model.addAttribute("category", category);
		return "category-create";
	}

	@PostMapping("/category/new")
	public String postMethodName(@ModelAttribute("category") Category category) {
		categoryService.createCategory(category);
		return "redirect:/admin/categories";
	}

	@GetMapping("/category/modify")
	public String showCategoryForm(@RequestParam(name = "categoryId", required = true) long categoryId, Model model) {
		Category category = categoryService.getCategoryById(categoryId)
				.orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable"));
		model.addAttribute("category", category);
		return "category-create";
	}
}
