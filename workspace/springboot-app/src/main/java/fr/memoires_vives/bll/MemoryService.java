package fr.memoires_vives.bll;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;

public interface MemoryService {
	List<Memory> findMemories();
	void createMemory(Memory memory, MultipartFile image, Boolean publish, Location location);
}
