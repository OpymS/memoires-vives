package fr.memoires_vives.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.dto.SearchCriteria;

@RestController
@RequestMapping("/api/memory")
public class MemoryRestController {

	private static final int PAGE_SIZE = 6;

	private final MemoryService memoryService;

	public MemoryRestController(MemoryService memoryService) {
		this.memoryService = memoryService;
	}

	@PostMapping("/grid")
	public ResponseEntity<?> sendPageOfMemories(@RequestBody SearchCriteria searchCriteria,
			@RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber) {
		Page<Memory> memories = memoryService.findMemoriesWithCriteria(PageRequest.of(pageNumber - 1, PAGE_SIZE), searchCriteria);
		return ResponseEntity.ok(memories);
	}
	
	@PostMapping("/map")
	public ResponseEntity<?> sendMapOfMemories(@RequestBody SearchCriteria searchCriteria) {
		List<Memory> memories = memoryService.findMemoriesOnMapWithCriteria(searchCriteria);
		return ResponseEntity.ok(memories);
	}
}
