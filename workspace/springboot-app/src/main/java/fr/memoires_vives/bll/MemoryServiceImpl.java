package fr.memoires_vives.bll;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.bo.MemoryVisibility;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.MemoryRepository;

@Primary
@Service
public class MemoryServiceImpl implements MemoryService {
	
	private final MemoryRepository memoryRepository;
	private final FileService fileService;
	private final LocationService locationService;
	private final UserService userService;

	public MemoryServiceImpl(MemoryRepository memoryRepository, FileService fileService, LocationService locationService, UserService userService) {
		this.memoryRepository = memoryRepository;
		this.fileService = fileService;
		this.locationService = locationService;
		this.userService = userService;
	}

	@Override
	public List<Memory> findMemories() {
		return memoryRepository.findAll();
	}

	@Override
	@Transactional
	public void createMemory(Memory memory, MultipartFile image, Boolean published, Location location) {
memory.setCreationDate(LocalDateTime.now());
		
		Location savedLocation = locationService.saveLocation(location);
		if (published == null || !published) {
			memory.setState(MemoryState.CREATED);
		} else {
			memory.setState(MemoryState.PUBLISHED);
		}
		User rememberer = userService.getCurrentUser();
		
		memory.setRememberer(rememberer);
		Hibernate.initialize(rememberer.getMemories());
		memory.setLocation(savedLocation);
		
		if(image != null && !image.isEmpty()) {
			try {
				memory.setMediaUUID(fileService.saveFile(image));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} else {
			memory.setMediaUUID(null);
		}
		memoryRepository.save(memory);
	}

	@Override
	public Memory getMemoryById(long memoryId) {
		return memoryRepository.findByMemoryId(memoryId);
	}

	@Override
	public boolean authorizedDisplay(Memory memory) {
		if (memory.getVisibility() == MemoryVisibility.PUBLIC) return true;
		User currentUser = userService.getCurrentUser();
		if (currentUser != null && memory.getRememberer().getUserId() == currentUser.getUserId())return true;
		if (memory.getVisibility() == MemoryVisibility.MEMBERS && currentUser != null) return true;
		if (memory.getVisibility() == MemoryVisibility.PRIVATE && currentUser != null && memory.getRememberer().getUserId() == currentUser.getUserId()) return true;
		return false;
	}

}
