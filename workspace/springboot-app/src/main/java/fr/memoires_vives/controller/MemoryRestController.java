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
import fr.memoires_vives.bll.MemoryUrlService;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.dto.MemoryView;
import fr.memoires_vives.dto.SearchCriteria;
import fr.memoires_vives.mapper.MemoryViewMapper;

@RestController
@RequestMapping("/api/memory")
public class MemoryRestController {

	private static final int PAGE_SIZE = 12;

	private final MemoryService memoryService;
	private final MemoryViewMapper memoryViewMapper;

	public MemoryRestController(MemoryService memoryService, MemoryUrlService memoryUrlService,
			MemoryViewMapper memoryViewMapper) {
		this.memoryService = memoryService;
		this.memoryViewMapper = memoryViewMapper;
	}

	@PostMapping("/grid")
	public ResponseEntity<?> sendPageOfMemories(@RequestBody SearchCriteria searchCriteria,
			@RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber) {
		Page<Memory> memories = memoryService.findMemoriesWithCriteria(PageRequest.of(pageNumber - 1, PAGE_SIZE),
				searchCriteria);
		Page<MemoryView> views = memories.map(memoryViewMapper::toView);
		return ResponseEntity.ok(views);
	}

	@PostMapping("/map")
	public ResponseEntity<?> sendMapOfMemories(@RequestBody SearchCriteria searchCriteria) {
		List<Memory> memories = memoryService.findMemoriesOnMapWithCriteria(searchCriteria);
		List<MemoryView> views = memories.stream().map(memoryViewMapper::toView).toList();
		return ResponseEntity.ok(views);
	}
}
