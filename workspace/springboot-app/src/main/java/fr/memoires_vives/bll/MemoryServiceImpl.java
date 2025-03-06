package fr.memoires_vives.bll;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.MemoryRepository;
import fr.memoires_vives.security.CustomUserDetails;

@Primary
@Service
public class MemoryServiceImpl implements MemoryService {

	private final MemoryRepository memoryRepository;
	private final FileService fileService;
	private final LocationService locationService;

	public MemoryServiceImpl(MemoryRepository memoryRepository, FileService fileService, LocationService locationService) {
		this.memoryRepository = memoryRepository;
		this.fileService = fileService;
		this.locationService = locationService;
	}

	@Override
	public List<Memory> findMemories() {
		return memoryRepository.findAll();
	}

	@Override
	public void createMemory(Memory memory, MultipartFile image, Boolean published, Location location) {
memory.setCreationDate(LocalDateTime.now());

		Location savedLocation = locationService.saveLocation(location);
		
		if (published) {
			memory.setState(MemoryState.PUBLISHED);
		} else {
			memory.setState(MemoryState.CREATED);
		}
		
//		User rememberer = new User();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		User rememberer = userDetails.getUser();
		memory.setRememberer(rememberer);
		
		memory.setLocation(savedLocation);
		
		try {
			memory.setMediaUUID(fileService.saveFile(image));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		memoryRepository.save(memory);
	}

}
