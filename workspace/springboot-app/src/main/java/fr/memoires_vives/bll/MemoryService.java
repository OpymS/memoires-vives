package fr.memoires_vives.bll;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.dto.SearchCriteria;

public interface MemoryService {
	Page<Memory> findMemories(Pageable pageable);
	Page<Memory> findMemoriesWithCriteria(Pageable pageable, SearchCriteria searchCriteria);
	Memory createMemory(Memory memory, MultipartFile image, Boolean publish, Location location);
	Memory updateMemory(Memory memoryWithUpdate, MultipartFile newImage, Boolean publish, Location locationWithUpdate);
	Memory getMemoryById(long memoryId);
	Memory getMemoryByImage(String mediaUUID);
	boolean authorizedDisplay(Memory memory);
	boolean authorizedModification(Memory memory);
	List<Memory> getMemoriesByCategory(Category category);
}
