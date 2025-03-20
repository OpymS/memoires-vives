package fr.memoires_vives.bll;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.bo.MemoryVisibility;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.repositories.CategoryRepository;
import fr.memoires_vives.repositories.MemoryRepository;

@Primary
@Service
public class MemoryServiceImpl implements MemoryService {

	private final MemoryRepository memoryRepository;
	private final FileService fileService;
	private final LocationService locationService;
	private final UserService userService;
	private final CategoryRepository categoryRepository;

	public MemoryServiceImpl(MemoryRepository memoryRepository, FileService fileService,
			LocationService locationService, UserService userService, CategoryRepository categoryRepository) {
		this.memoryRepository = memoryRepository;
		this.fileService = fileService;
		this.locationService = locationService;
		this.userService = userService;
		this.categoryRepository = categoryRepository;
	}

	@Override
	public List<Memory> findMemories() {
		return memoryRepository.findAll();
	}

	@Override
	@Transactional
	public Memory createMemory(Memory memory, MultipartFile image, Boolean published, Location location) {
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

		if (image != null && !image.isEmpty()) {
			try {
				memory.setMediaUUID(fileService.saveFile(image));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			memory.setMediaUUID(null);
		}
		Category category = categoryRepository.findByName("Default category");
		memory.setCategory(category);
		
		return memoryRepository.save(memory);
	}

	@Override
	public Memory getMemoryById(long memoryId) {
		return memoryRepository.findByMemoryId(memoryId);
	}

	@Override
	public boolean authorizedDisplay(Memory memory) {
		if (memory.getVisibility() == MemoryVisibility.PUBLIC)
			return true;
		if (userService.isAdmin())
			return true;
		User currentUser = userService.getCurrentUser();
		if (currentUser != null && memory.getRememberer().getUserId() == currentUser.getUserId())
			return true;
		if (memory.getVisibility() == MemoryVisibility.MEMBERS && currentUser != null)
			return true;
		if (memory.getVisibility() == MemoryVisibility.PRIVATE && currentUser != null
				&& memory.getRememberer().getUserId() == currentUser.getUserId())
			return true;
		return false;
	}

	@Override
	public boolean authorizedModification(Memory memory) {
		if (userService.isAdmin())
			return true;
		User currentUser = userService.getCurrentUser();
		long remembererId;
		if (memory.getRememberer() == null) {
			remembererId = this.getMemoryById(memory.getMemoryId()).getRememberer().getUserId();
		} else {
			remembererId = memory.getRememberer().getUserId();
		}
		if (currentUser != null && remembererId == currentUser.getUserId())
			return true;
		return false;
	}

	@Override
	public Memory updateMemory(Memory memoryWithUpdate, MultipartFile newImage, Boolean publish,
			Location locationWithUpdate) {
		
		Memory existingMemoryToUpdate = memoryRepository.findByMemoryId(memoryWithUpdate.getMemoryId());
		Location existingLocation = locationService.getById(existingMemoryToUpdate.getLocation().getLocationId());
		
		if (memoryWithUpdate.getTitle() != "" && existingMemoryToUpdate.getTitle() != memoryWithUpdate.getTitle()) {
			existingMemoryToUpdate.setTitle(memoryWithUpdate.getTitle());			
		}
		
		if (memoryWithUpdate.getDescription() != ""
				&& existingMemoryToUpdate.getDescription() != memoryWithUpdate.getDescription()) {
			existingMemoryToUpdate.setDescription(memoryWithUpdate.getDescription());			
		}
		
		if (memoryWithUpdate.getMemoryDate() != null
				&& existingMemoryToUpdate.getMemoryDate() != memoryWithUpdate.getMemoryDate()) {
			existingMemoryToUpdate.setMemoryDate(memoryWithUpdate.getMemoryDate());			
		}
		
		existingMemoryToUpdate.setModificationDate(LocalDateTime.now());
		
		if (memoryWithUpdate.getVisibility() != null
				&& existingMemoryToUpdate.getVisibility() != memoryWithUpdate.getVisibility()) {
			existingMemoryToUpdate.setVisibility(memoryWithUpdate.getVisibility());			
		}
		
		if (publish == null || !publish) {
			existingMemoryToUpdate.setState(MemoryState.CREATED);
		} else {
			existingMemoryToUpdate.setState(MemoryState.PUBLISHED);
		}

		if (newImage != null && !newImage.isEmpty()) {
			try {
				fileService.deleteFile(existingMemoryToUpdate.getMediaUUID());
				existingMemoryToUpdate.setMediaUUID(fileService.saveFile(newImage));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (existingLocation.getMemories().size() <= 1) {
			locationWithUpdate.setLocationId(existingLocation.getLocationId());
			locationService.saveLocation(locationWithUpdate);
		} else if (locationWithUpdate.getName() != existingLocation.getName()
				|| locationWithUpdate.getLatitude() != existingLocation.getLatitude()
				|| locationWithUpdate.getLongitude() != existingLocation.getLongitude()) {
			Location newLocation = locationService.saveLocation(locationWithUpdate);
			existingMemoryToUpdate.setLocation(newLocation);
		}
		System.out.println(existingMemoryToUpdate);
		return memoryRepository.save(existingMemoryToUpdate);
	}

	@Override
	public Memory getMemoryByImage(String mediaUUID) {
		return memoryRepository.findByMediaUUID(mediaUUID);
	}

}
