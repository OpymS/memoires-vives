package fr.memoires_vives.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.Memory;

@RestController
@RequestMapping("/api/category")
public class CategoryRestController {

	private final MemoryService memoryService;

	public CategoryRestController(MemoryService memoryService) {
		this.memoryService = memoryService;
	}

	@GetMapping("/{categoryId}/associatedMemories")
	public ResponseEntity<?> getMemoriesByCategory(@PathVariable(name = "categoryId") long categoryId) {
		List<Memory> memories = memoryService.getMemoriesByCategoryForAdmin(categoryId);
		return ResponseEntity.ok(memories);
	}

}
