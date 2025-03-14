package fr.memoires_vives.bll;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;

public interface MemoryService {
	List<Memory> findMemories();
	Memory createMemory(Memory memory, MultipartFile image, Boolean publish, Location location);
	Memory updateMemory(Memory memoryWithUpdate, MultipartFile newImage, Boolean publish, Location locationWithUpdate);
	Memory getMemoryById(long memoryId);
	Memory getMemoryByImage(String mediaUUID);
	boolean authorizedDisplay(Memory memory);
	boolean authorizedModification(Memory memory);
}
