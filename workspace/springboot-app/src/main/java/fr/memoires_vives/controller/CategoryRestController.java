package fr.memoires_vives.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Memory;

@RestController
@RequestMapping("/api/category")
public class CategoryRestController {
	private final UserService userService;
	private final CategoryService categoryService;
	private final MemoryService memoryService;

	public CategoryRestController(UserService userService, CategoryService categoryService, MemoryService memoryService) {
		this.userService = userService;
		this.categoryService = categoryService;
		this.memoryService = memoryService;
	}

	@GetMapping("/{categoryId}/associatedMemories")
	public ResponseEntity<?> getMemoriesByCategory(@PathVariable(name="categoryId") long categoryId) {
		if (!userService.isAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access forbidden.");
		}
		Optional<Category> optionalCategory = Optional.ofNullable(categoryService.getCategoryById(categoryId));
		if (optionalCategory.isPresent()) {
			Category category = optionalCategory.get();
			List<Memory> memories = memoryService.getMemoriesByCategory(category);
			return ResponseEntity.ok(memories);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found.");
		}
	}

}
