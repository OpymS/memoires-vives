package fr.memoires_vives.mapper;

import org.springframework.stereotype.Component;

import fr.memoires_vives.bll.MemoryUrlService;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.dto.MemoryView;

@Component
public class MemoryViewMapper {

	private final MemoryUrlService memoryUrlService;

	public MemoryViewMapper(MemoryUrlService memoryUrlService) {
		this.memoryUrlService = memoryUrlService;
	}

	public MemoryView toView(Memory memory) {
		return new MemoryView(memory, memoryUrlService.buildCanonicalUrl(memory));
	}
}
