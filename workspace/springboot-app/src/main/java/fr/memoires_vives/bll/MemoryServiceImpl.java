package fr.memoires_vives.bll;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import fr.memoires_vives.exception.DataPersistenceException;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.FileStorageException;
import fr.memoires_vives.exception.UnauthorizedActionException;
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
	private final CategoryService categoryService;

	public MemoryServiceImpl(MemoryRepository memoryRepository, FileService fileService,
			LocationService locationService, UserService userService, CategoryService categoryService) {
		this.memoryRepository = memoryRepository;
		this.fileService = fileService;
		this.locationService = locationService;
		this.userService = userService;
		this.categoryService = categoryService;
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
	public Page<Memory> findMemoriesWithCriteria(Pageable pageable, SearchCriteria searchCriteria) {
		Specification<Memory> specification = createSpecification(searchCriteria);

		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				Sort.by(Sort.Direction.DESC, "memoryId"));

		return memoryRepository.findAll(specification, sortedPageable);
	}

	@Override
	public List<Memory> findMemoriesOnMapWithCriteria(SearchCriteria searchCriteria) {
		Specification<Memory> baseSpecification = createSpecification(searchCriteria);
		Specification<Memory> geographicSpecification = getGeographicSpecification(searchCriteria);

		Specification<Memory> globalSpecification = Specification.where(baseSpecification).and(geographicSpecification);

		return memoryRepository.findAll(globalSpecification);
	}

	@Override
	public Memory getMemoryById(Long memoryId) {
		Memory memory = memoryRepository.findById(memoryId)
				.orElseThrow(() -> new EntityNotFoundException("Souvenir introuvable"));
		assertUserCanSee(memory);
		return memory;

	}

	@Override
	public Memory getMemoryForModification(long memoryId) {
		Memory memory = memoryRepository.findById(memoryId)
				.orElseThrow(() -> new EntityNotFoundException("Souvenir introuvable"));
		assertUserCanModify(memory);
		return memory;
	}

	@Override
	public Memory getMemoryByImage(String mediaUUID) {
		return memoryRepository.findByMediaUUID(mediaUUID);
	}

	@Override
	@Transactional
	public Memory createMemory(Memory memory, MultipartFile image, Boolean published, Location location) {
		User rememberer = userService.getCurrentUser();
		if (rememberer == null) {
			throw new UnauthorizedActionException("Vous devez vous connecter pour ajouter un souvenir");
		}

		memory.setCreationDate(LocalDateTime.now());
		updateVisibility(memory);
		updateState(memory, published);

		Location savedLocation = locationService.saveLocation(location);
		memory.setLocation(savedLocation);

		memory.setRememberer(rememberer);
		Hibernate.initialize(rememberer.getMemories());

		updateMedia(memory, image);

		return saveMemory(memory);
	}

	@Override
	@Transactional
	public Memory updateMemory(Memory updatedData, MultipartFile newImage, Boolean publish,
			Location locationWithUpdate) {

		Memory existingMemory = fetchExistingMemory(updatedData.getMemoryId());

		assertUserCanModify(existingMemory);

		updateBasicFields(existingMemory, updatedData);

		updateState(existingMemory, publish);

		updateMedia(existingMemory, newImage);

		updateLocation(existingMemory, locationWithUpdate);

		return saveMemory(existingMemory);
	}

	@Override
	public List<Memory> getMemoriesByCategory(Category category) {
		return memoryRepository.findByCategoryId(category.getCategoryId());
	}

	@Override
	public List<Memory> getMemoriesByCategoryForAdmin(long categoryId) {
		if (!userService.isAdmin()) {
			throw new UnauthorizedActionException("Access forbidden");
		}
		Category category = categoryService.getCategoryById(categoryId);
		if (category == null) {
			throw new EntityNotFoundException("Category not found");
		}
		return memoryRepository.findByCategoryId(categoryId);
	}

//	Les méthodes privées

	private Specification<Memory> createSpecification(SearchCriteria criteria) {
		// TODO Gérer la visibilité. Pas critique pour l'instant car tous les souvenirs
		// sont publics dès qu'ils sont publiés.
		return (Root<Memory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			addWordsPredicates(predicates, root, cb, criteria);
			addDatePredicates(predicates, root, cb, criteria);
			addCategoryPredicate(predicates, root, cb, criteria);

			User user = userService.getCurrentUser();
			addVisibilityPredicates(predicates, root, cb, criteria, user);

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	private void addWordsPredicates(List<Predicate> predicates, Root<Memory> root, CriteriaBuilder cb,
			SearchCriteria criteria) {
		if (criteria.getWords() == null || criteria.getWords().isEmpty()) {
			return;
		}

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

	private void addDatePredicates(List<Predicate> predicates, Root<Memory> root, CriteriaBuilder cb,
			SearchCriteria criteria) {
		if (criteria.getAfter() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("memoryDate"), criteria.getAfter()));
		}

		if (criteria.getBefore() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("memoryDate"), criteria.getBefore()));
		}
	}

	private void addCategoryPredicate(List<Predicate> predicates, Root<Memory> root, CriteriaBuilder cb,
			SearchCriteria criteria) {
		if (criteria.getCategoriesId() == null || criteria.getCategoriesId().isEmpty()) {
			return;
		}
		List<Predicate> categoriesPredicates = new ArrayList<>();
		for (long categoryId : criteria.getCategoriesId()) {
			categoriesPredicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
		}
		predicates.add(cb.or(categoriesPredicates.toArray(new Predicate[0])));
	}

	private void addVisibilityPredicates(List<Predicate> predicates, Root<Memory> root, CriteriaBuilder cb,
			SearchCriteria criteria, User user) {
		if (criteria.isOnlyMine() && user != null) {
			predicates.add(cb.equal(root.get("rememberer").get("userId"), user.getUserId()));
			if (criteria.getStatus() == 2) {
				predicates.add(cb.equal(root.get("state"), MemoryState.PUBLISHED));
			}

			if (criteria.getStatus() == 3) {
				predicates.add(cb.equal(root.get("state"), MemoryState.CREATED));
			}
		} else {
			Predicate published = cb.equal(root.get("state"), MemoryState.PUBLISHED);

			if (user != null) {
				Predicate mine = cb.equal(root.get("rememberer").get("userId"), user.getUserId());
				predicates.add(cb.or(published, mine));
			} else {
				predicates.add(published);
			}
		}
	}

	private Specification<Memory> getGeographicSpecification(SearchCriteria criteria) {
		double north = criteria.getNorth();
		double south = criteria.getSouth();
		double east = criteria.getEast();
		double west = criteria.getWest();

		if (east - west >= 300) {
			east = 180;
			west = -180;
		}

		while (west < -180) {
			west += 360;
		}
		while (east > 180) {
			east -= 360;
		}

		final double innerEast = east;
		final double innerWest = west;

		return (Root<Memory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.greaterThanOrEqualTo(root.get("location").get("latitude"), south));
			predicates.add(cb.lessThanOrEqualTo(root.get("location").get("latitude"), north));
			if (innerWest < innerEast) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("location").get("longitude"), innerWest));
				predicates.add(cb.lessThanOrEqualTo(root.get("location").get("longitude"), innerEast));
			} else {
				List<Predicate> longitudesPredicates = new ArrayList<>();
				longitudesPredicates.add(cb.greaterThanOrEqualTo(root.get("location").get("longitude"), innerWest));
				longitudesPredicates.add(cb.lessThanOrEqualTo(root.get("location").get("longitude"), innerEast));
				predicates.add(cb.or(longitudesPredicates.toArray(new Predicate[0])));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	private void assertUserCanModify(Memory memory) {
		User currentUser = userService.getCurrentUser();
		if (currentUser == null) {
			throw new UnauthorizedActionException("Vous devez être connecté pour modifier un souvenir.");
		}

		User rememberer = memory.getRememberer();
		if (!userService.isAdmin() && rememberer.getUserId() != currentUser.getUserId()) {
			throw new UnauthorizedActionException(
					"Vous n'êtes pas autorisé à modifier ce souvenir car vous n'en êtes pas l'auteur·e.");
		}
	}

	private void assertUserCanSee(Memory memory) {
		User currentUser = userService.getCurrentUser();
		User rememberer = memory.getRememberer();
		boolean notOwner = currentUser == null || rememberer.getUserId() != currentUser.getUserId();
		if (!userService.isAdmin()) {
			if (memory.getVisibility().equals(MemoryVisibility.PRIVATE) && notOwner) {
				throw new UnauthorizedActionException(
						"Vous n'êtes pas autorisé à voir ce souvenir. Souvenir privé dont vous n'êtes pas l@ propriétaire");
			}
			if (memory.getVisibility().equals(MemoryVisibility.MEMBERS) && currentUser == null) {
				throw new UnauthorizedActionException(
						"Vous n'êtes pas autorisé à voir ce souvenir. Son affichage est réservé aux membres inscrit·e·s");
			}
			if ((memory.getState().equals(MemoryState.CREATED) || memory.getState().equals(MemoryState.DELETED))
					&& notOwner) {
				throw new UnauthorizedActionException(
						"Vous n'êtes pas autorisé à voir ce souvenir. Souvenir non publié dont vous n'êtes pas l@ propriétaire");
			}
		}
	}

	private Memory fetchExistingMemory(long memoryId) {
		return memoryRepository.findById(memoryId)
				.orElseThrow(() -> new EntityNotFoundException("Souvenir introuvable."));
	}

	private void updateBasicFields(Memory existingMemory, Memory updatedData) {
		if (StringUtils.isNotBlank(updatedData.getTitle())
				&& !updatedData.getTitle().equals(existingMemory.getTitle())) {
			existingMemory.setTitle(updatedData.getTitle());
		}

		if (StringUtils.isNotBlank(updatedData.getDescription())
				&& !updatedData.getDescription().equals(existingMemory.getDescription())) {
			existingMemory.setDescription(updatedData.getDescription());
		}

		if (updatedData.getMemoryDate() != null
				&& !updatedData.getMemoryDate().equals(existingMemory.getMemoryDate())) {
			existingMemory.setMemoryDate(updatedData.getMemoryDate());
		}

		if (updatedData.getCategory() != null && !updatedData.getCategory().equals(existingMemory.getCategory())) {
			existingMemory.setCategory(updatedData.getCategory());
		}

		if (updatedData.getVisibility() != null
				&& !updatedData.getVisibility().equals(existingMemory.getVisibility())) {
			existingMemory.setVisibility(updatedData.getVisibility());
		}

		existingMemory.setModificationDate(LocalDateTime.now());
	}

	private void updateState(Memory memory, Boolean publish) {
		boolean shouldPublish = publish != null && publish;
		memory.setState(shouldPublish ? MemoryState.PUBLISHED : MemoryState.CREATED);
	}

	private void updateMedia(Memory memory, MultipartFile newImage) {
		if (newImage == null || newImage.isEmpty()) {
			return;
		}
		try {
			if (memory.getMemoryId() != 0) {
				fileService.deleteFile(memory.getMediaUUID());
			}
			memory.setMediaUUID(fileService.saveFile(newImage));
		} catch (FileStorageException e) {
			if (memory.getMemoryId() == 0) {
				memory.setMediaUUID(null);
			}
			// en update, on ne fait rien, on laisse l'ancienne image
		}
	}

	private void updateLocation(Memory memory, Location locationWithUpdate) {
		Location existingLocation = locationService.getById(memory.getLocation().getLocationId());

		boolean isDifferent = !existingLocation.getName().equals(locationWithUpdate.getName())
				|| Double.compare(existingLocation.getLatitude(), locationWithUpdate.getLatitude()) != 0
				|| Double.compare(existingLocation.getLongitude(), locationWithUpdate.getLongitude()) != 0;

		if (existingLocation.getMemories().size() <= 1) {
			locationWithUpdate.setLocationId(existingLocation.getLocationId());
			Location updatedLocation = locationService.saveLocation(locationWithUpdate);
			memory.setLocation(updatedLocation);
		} else if (isDifferent) {
			Location newLocation = locationService.saveLocation(locationWithUpdate);
			memory.setLocation(newLocation);
		}
	}

	private void updateVisibility(Memory memory) {
		memory.setVisibility(MemoryVisibility.PUBLIC);
	}

	private Memory saveMemory(Memory memory) {
		try {
			return memoryRepository.save(memory);
		} catch (DataAccessException e) {
			throw new DataPersistenceException("Problème lors de l'enregistrement en base de données", e);
		}
	}
}
