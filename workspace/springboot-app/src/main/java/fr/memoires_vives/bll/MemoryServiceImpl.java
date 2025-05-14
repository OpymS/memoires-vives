package fr.memoires_vives.bll;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.bo.MemoryVisibility;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.dto.SearchCriteria;
import fr.memoires_vives.repositories.MemoryRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Primary
@Service
public class MemoryServiceImpl implements MemoryService {

	private final MemoryRepository memoryRepository;
	private final FileService fileService;
	private final LocationService locationService;
	private final UserService userService;

	public MemoryServiceImpl(MemoryRepository memoryRepository, FileService fileService,
			LocationService locationService, UserService userService) {
		this.memoryRepository = memoryRepository;
		this.fileService = fileService;
		this.locationService = locationService;
		this.userService = userService;
	}

	@Override
	public Page<Memory> findMemories(Pageable pageable) {
		List<Memory> memoriesList = memoryRepository.findAll();

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int firstMemory = currentPage * pageSize;

		List<Memory> shortMemoriesList;

		if (memoriesList.size() < firstMemory) {
			shortMemoriesList = Collections.emptyList();
		} else {
			int endIndex = Math.min(firstMemory + pageSize, memoriesList.size());
			shortMemoriesList = memoriesList.subList(firstMemory, endIndex);
		}

		Page<Memory> memoriesPage = new PageImpl<Memory>(shortMemoriesList, pageable, memoriesList.size());

		return memoriesPage;
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
		memory.setVisibility(MemoryVisibility.PUBLIC);

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

		if (memoryWithUpdate.getCategory() != null
				&& existingMemoryToUpdate.getCategory() != memoryWithUpdate.getCategory()) {
			existingMemoryToUpdate.setCategory(memoryWithUpdate.getCategory());
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

	@Override
	public List<Memory> getMemoriesByCategory(Category category) {
		return memoryRepository.findByCategoryId(category.getCategoryId());
	}

	@Override
	public Page<Memory> findMemoriesWithCriteria(Pageable pageable, SearchCriteria searchCriteria) {
		List<Memory> memoriesList = memoryRepository.findAll(createSpecification(searchCriteria));
		
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;
		List<Memory> shortMemoriesList;
		
		if (memoriesList.size()< startItem) {
			shortMemoriesList = Collections.emptyList();
		}else {
			int endIndex = Math.min(startItem+pageSize, memoriesList.size());
			shortMemoriesList = memoriesList.subList(startItem, endIndex);
		}
		
		Page<Memory> memoriesPage = new PageImpl<Memory>(shortMemoriesList, PageRequest.of(currentPage, pageSize), memoriesList.size());
		
		// TODO Gérer les cas des souvenirs privés
		return memoriesPage;
	}

	private Specification<Memory> createSpecification(SearchCriteria criteria) {
		return (Root<Memory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (criteria.getWords() != null && !criteria.getWords().isEmpty()) {
				List<Predicate> wordPredicates = new ArrayList<>();
				for (String word : criteria.getWords()) {
					if (criteria.isTitleOnly()) {
						wordPredicates.add(cb.like(root.get("title"), "%" + word + "%"));

					} else {
						Predicate titlePredicate = cb.like(root.get("title"), "%" + word + "%");
						Predicate descriptionPredicate = cb.like(root.get("description"), "%" + word + "%");
						wordPredicates.add(cb.or(titlePredicate, descriptionPredicate));
					}
				}
				predicates.add(cb.and(wordPredicates.toArray(new Predicate[0])));
			}

			if (criteria.getAfter() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("memoryDate"), criteria.getAfter()));
			}

			if (criteria.getBefore() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("memoryDate"), criteria.getBefore()));
			}

			if (criteria.getCategoriesId() != null && !criteria.getCategoriesId().isEmpty()) {
				List<Predicate> categoriesPredicates = new ArrayList<>();
				for (long categoryId : criteria.getCategoriesId()) {
					categoriesPredicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
				}
				predicates.add(cb.or(categoriesPredicates.toArray(new Predicate[0])));
			}

			if (criteria.isOnlyMine()) {
				User user = userService.getCurrentUser();
				if (user != null) {
					predicates.add(cb.equal(root.get("rememberer").get("userId"), user.getUserId()));					
					if (criteria.getStatus() == 2) {
						predicates.add(cb.equal(root.get("state"), MemoryState.PUBLISHED));
					}
					
					if (criteria.getStatus() == 3) {
						predicates.add(cb.equal(root.get("state"), MemoryState.CREATED));
					}
				}
			} else {
				predicates.add(cb.equal(root.get("state"), MemoryState.PUBLISHED));
			}


			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	@Override
	public List<Memory> findMemoriesOnMapWithCriteria(SearchCriteria searchCriteria) {
		// TODO Auto-generated method stub
		return memoryRepository.findAll();
	}
}
