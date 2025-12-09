package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.bo.MemoryVisibility;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.dto.SearchCriteria;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.FileStorageException;
import fr.memoires_vives.exception.UnauthorizedActionException;
import fr.memoires_vives.repositories.MemoryRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
public class MemoryServiceImplTest {

	@Mock
	private MemoryRepository memoryRepository;

	@Mock
	private FileService fileService;

	@Mock
	private LocationService locationService;

	@Mock
	private UserService userService;

	@Mock
	private CategoryService categoryService;

	@Mock
	private MultipartFile imageFile;

	@InjectMocks
	private MemoryServiceImpl memoryService;

	private List<Memory> buildMemories(int count) {
		List<Memory> memories = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			memories.add(new Memory());
		}
		return memories;
	}

	@SuppressWarnings("unchecked")
	private static Class<Specification<Memory>> specificationClass() {
		return (Class<Specification<Memory>>) (Class<?>) Specification.class;
	}

	private User makeUser(long id) {
		User u = new User();
		u.setUserId(id);
		return u;
	}

	private Category makeCategory(Long id) {
		Category c = new Category();
		c.setCategoryId(id);
		return c;
	}

	private Location makeLocation(Long id) {
		Location l = new Location();
		l.setLocationId(id);
		l.setName("loc-" + (id == null ? "null" : id));
		l.setLatitude(10.0);
		l.setLongitude(20.0);
		l.setMemories(new ArrayList<>());
		return l;
	}

	private Memory makeMemory(Long memId, User rememberer, Location loc) {
		Memory m = new Memory();
		if (memId != null)
			m.setMemoryId(memId);
		m.setRememberer(rememberer);
		m.setLocation(loc);
		m.setState(MemoryState.CREATED);
		m.setVisibility(MemoryVisibility.PUBLIC);
		m.setCategory(makeCategory(1L));
		m.setTitle("t");
		m.setDescription("d");
		m.setMediaUUID(null);
		return m;
	}

//  Tests de findMemories

	@Test
	void findMemories_shouldReturnFirstPage() {
		List<Memory> allMemories = buildMemories(10);
		when(memoryRepository.findAll()).thenReturn(allMemories);

		Pageable pageable = PageRequest.of(0, 3);

		Page<Memory> page = memoryService.findMemories(pageable);

		assertEquals(3, page.getNumberOfElements());
		assertEquals(0, page.getNumber());
		assertEquals(3, page.getSize());
		assertEquals(10, page.getTotalElements());
		assertEquals(4, page.getTotalPages());

		assertSame(allMemories.get(0), page.getContent().get(0));
		assertSame(allMemories.get(1), page.getContent().get(1));
		assertSame(allMemories.get(2), page.getContent().get(2));

		verify(memoryRepository).findAll();
	}

	@Test
	void findMemories_shouldReturnSecondPage() {
		List<Memory> allMemories = buildMemories(10);
		when(memoryRepository.findAll()).thenReturn(allMemories);

		Pageable pageable = PageRequest.of(1, 3);

		Page<Memory> page = memoryService.findMemories(pageable);

		assertEquals(3, page.getNumberOfElements());
		assertEquals(1, page.getNumber());
		assertEquals(3, page.getSize());
		assertEquals(10, page.getTotalElements());

		assertSame(allMemories.get(3), page.getContent().get(0));
		assertSame(allMemories.get(4), page.getContent().get(1));
		assertSame(allMemories.get(5), page.getContent().get(2));

		verify(memoryRepository).findAll();
	}

	@Test
	void findMemories_lastPageShouldContainRemainingElements() {
		List<Memory> allMemories = buildMemories(10);
		when(memoryRepository.findAll()).thenReturn(allMemories);

		Pageable pageable = PageRequest.of(3, 3);

		Page<Memory> page = memoryService.findMemories(pageable);

		assertEquals(1, page.getNumberOfElements());
		assertEquals(3, page.getNumber());
		assertEquals(3, page.getSize());
		assertEquals(10, page.getTotalElements());

		assertSame(allMemories.get(9), page.getContent().get(0));

		verify(memoryRepository).findAll();
	}

	@Test
	void findMemories_pageBeyondEndShouldReturnEmptyPage() {
		List<Memory> allMemories = buildMemories(10);
		when(memoryRepository.findAll()).thenReturn(allMemories);

		Pageable pageable = PageRequest.of(5, 3);

		Page<Memory> page = memoryService.findMemories(pageable);

		assertTrue(page.getContent().isEmpty());
		assertEquals(0, page.getNumberOfElements());
		assertEquals(5, page.getNumber());
		assertEquals(3, page.getSize());
		assertEquals(10, page.getTotalElements());

		verify(memoryRepository).findAll();
	}

	@Test
	void findMemories_exactBoundaryShouldWork() {
		List<Memory> allMemories = buildMemories(6);
		when(memoryRepository.findAll()).thenReturn(allMemories);

		Pageable pageable = PageRequest.of(1, 3);

		Page<Memory> page = memoryService.findMemories(pageable);

		assertEquals(3, page.getNumberOfElements());
		assertEquals(1, page.getNumber());
		assertEquals(3, page.getSize());
		assertEquals(6, page.getTotalElements());

		assertSame(allMemories.get(3), page.getContent().get(0));
		assertSame(allMemories.get(4), page.getContent().get(1));
		assertSame(allMemories.get(5), page.getContent().get(2));

		verify(memoryRepository).findAll();
	}

// Tests de findMemoriesWithCriteria

	@Test
	void findMemoriesWithCriteria_shouldUseSortedPageableAndSpecification() {
		Pageable originalPageable = PageRequest.of(1, 5, Sort.by("title").ascending());
		SearchCriteria criteria = new SearchCriteria();

		Page<Memory> expectedPage = new PageImpl<>(List.of(new Memory()), originalPageable, 1);

		when(memoryRepository.findAll(Mockito.<Specification<Memory>>any(), any(Pageable.class)))
				.thenReturn(expectedPage);

		Page<Memory> result = memoryService.findMemoriesWithCriteria(originalPageable, criteria);

		assertSame(expectedPage, result);

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(memoryRepository).findAll(Mockito.<Specification<Memory>>any(), pageableCaptor.capture());

		Pageable usedPageable = pageableCaptor.getValue();
		assertEquals(1, usedPageable.getPageNumber());
		assertEquals(5, usedPageable.getPageSize());

		Sort.Order order = usedPageable.getSort().getOrderFor("memoryId");
		assertNotNull(order);
		assertEquals(Sort.Direction.DESC, order.getDirection());
		assertEquals(1, usedPageable.getSort().toList().size());
	}

	@Test
	void createSpecification_onlyMineStatus2_shouldAddMineAndPublishedPredicates() {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setOnlyMine(true);
		criteria.setStatus(2); // PUBLISHED

		Pageable pageable = PageRequest.of(0, 10);

		ArgumentCaptor<Specification<Memory>> specCaptor = ArgumentCaptor.forClass(specificationClass());

		when(memoryRepository.findAll(specCaptor.capture(), any(Pageable.class))).thenReturn(Page.empty(pageable));

		User user = makeUser(123L);
		when(userService.getCurrentUser()).thenReturn(user);

		memoryService.findMemoriesWithCriteria(pageable, criteria);

		Specification<Memory> spec = specCaptor.getValue();
		assertNotNull(spec);

		@SuppressWarnings("unchecked")
		Root<Memory> root = mock(Root.class);
		CriteriaQuery<?> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);

		@SuppressWarnings("unchecked")
		Path<Object> remembererPath = mock(Path.class);
		@SuppressWarnings("unchecked")
		Path<Object> remembererUserIdPath = mock(Path.class);

		when(root.get("rememberer")).thenReturn(remembererPath);
		when(remembererPath.get("userId")).thenReturn(remembererUserIdPath);

		@SuppressWarnings("unchecked")
		Path<Object> statePath = mock(Path.class);
		when(root.get("state")).thenReturn(statePath);

		Predicate minePredicate = mock(Predicate.class);
		Predicate publishedPredicate = mock(Predicate.class);
		Predicate finalAndPredicate = mock(Predicate.class);

		when(cb.equal(remembererUserIdPath, user.getUserId())).thenReturn(minePredicate);
		when(cb.equal(statePath, MemoryState.PUBLISHED)).thenReturn(publishedPredicate);

		when(cb.and(any(Predicate[].class))).thenReturn(finalAndPredicate);

		Predicate result = spec.toPredicate(root, query, cb);
		assertSame(finalAndPredicate, result);

		verify(cb).equal(remembererUserIdPath, user.getUserId());
		verify(cb).equal(statePath, MemoryState.PUBLISHED);
	}

	@Test
	void createSpecification_userLoggedInNotOnlyMine_shouldAddPublishedOrMinePredicate() {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setOnlyMine(false);

		Pageable pageable = PageRequest.of(0, 10);

		ArgumentCaptor<Specification<Memory>> specCaptor = ArgumentCaptor.forClass(specificationClass());

		when(memoryRepository.findAll(specCaptor.capture(), any(Pageable.class))).thenReturn(Page.empty(pageable));

		User user = makeUser(456L);
		when(userService.getCurrentUser()).thenReturn(user);

		memoryService.findMemoriesWithCriteria(pageable, criteria);

		Specification<Memory> spec = specCaptor.getValue();
		assertNotNull(spec);

		@SuppressWarnings("unchecked")
		Root<Memory> root = mock(Root.class);
		CriteriaQuery<?> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);

		@SuppressWarnings("unchecked")
		Path<Object> remembererPath = mock(Path.class);
		@SuppressWarnings("unchecked")
		Path<Object> remembererUserIdPath = mock(Path.class);

		when(root.get("rememberer")).thenReturn(remembererPath);
		when(remembererPath.get("userId")).thenReturn(remembererUserIdPath);

		@SuppressWarnings("unchecked")
		Path<Object> statePath = mock(Path.class);
		when(root.get("state")).thenReturn(statePath);

		Predicate publishedPredicate = mock(Predicate.class);
		Predicate minePredicate = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Predicate finalAndPredicate = mock(Predicate.class);

		when(cb.equal(statePath, MemoryState.PUBLISHED)).thenReturn(publishedPredicate);
		when(cb.equal(remembererUserIdPath, user.getUserId())).thenReturn(minePredicate);

		when(cb.or(publishedPredicate, minePredicate)).thenReturn(orPredicate);
		when(cb.and(any(Predicate[].class))).thenReturn(finalAndPredicate);

		Predicate result = spec.toPredicate(root, query, cb);
		assertSame(finalAndPredicate, result);

		verify(cb).equal(statePath, MemoryState.PUBLISHED);
		verify(cb).equal(remembererUserIdPath, user.getUserId());
		verify(cb).or(publishedPredicate, minePredicate);
	}

//  Tests de findMemoriesOnMapWithCriteria

	@Test
	void findMemoriesOnMapWithCriteria_shouldCallRepositoryWithCombinedSpecification() {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setNorth(50.0);
		criteria.setSouth(40.0);
		criteria.setWest(-5.0);
		criteria.setEast(5.0);

		List<Memory> expected = List.of(new Memory(), new Memory());
		when(memoryRepository.findAll(Mockito.<Specification<Memory>>any())).thenReturn(expected);

		List<Memory> result = memoryService.findMemoriesOnMapWithCriteria(criteria);

		assertSame(expected, result);

		ArgumentCaptor<Specification<Memory>> specCaptor = ArgumentCaptor.forClass(specificationClass());
		verify(memoryRepository).findAll(specCaptor.capture());

		Specification<Memory> usedSpec = specCaptor.getValue();
		assertNotNull(usedSpec);
	}

	@SuppressWarnings("unchecked")
	@Test
	void findMemoriesOnMapWithCriteria_normalBox_shouldUseLatitudeAndLongitudeRanges() {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setNorth(50.0);
		criteria.setSouth(40.0);
		criteria.setWest(-5.0);
		criteria.setEast(5.0);

		ArgumentCaptor<Specification<Memory>> specCaptor = ArgumentCaptor.forClass(specificationClass());

		when(memoryRepository.findAll(specCaptor.capture())).thenReturn(List.of());

		when(userService.getCurrentUser()).thenReturn(null);

		memoryService.findMemoriesOnMapWithCriteria(criteria);

		Specification<Memory> spec = specCaptor.getValue();
		assertNotNull(spec);

		@SuppressWarnings("rawtypes")
		Root root = mock(Root.class);
		CriteriaQuery<?> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);

		@SuppressWarnings("rawtypes")
		Path locationPath = mock(Path.class);
		@SuppressWarnings("rawtypes")
		Path latitudePath = mock(Path.class);
		@SuppressWarnings("rawtypes")
		Path longitudePath = mock(Path.class);

		// lenient pour éviter les problèmes avec createSpecification qui appelle
		// root.get("state"), etc.
		lenient().when(root.get("location")).thenReturn(locationPath);
		lenient().when(locationPath.get("latitude")).thenReturn(latitudePath);
		lenient().when(locationPath.get("longitude")).thenReturn(longitudePath);

		Predicate latGe = mock(Predicate.class);
		Predicate latLe = mock(Predicate.class);
		Predicate lonGe = mock(Predicate.class);
		Predicate lonLe = mock(Predicate.class);

		when(cb.greaterThanOrEqualTo(latitudePath, 40.0)).thenReturn(latGe);
		when(cb.lessThanOrEqualTo(latitudePath, 50.0)).thenReturn(latLe);
		when(cb.greaterThanOrEqualTo(longitudePath, -5.0)).thenReturn(lonGe);
		when(cb.lessThanOrEqualTo(longitudePath, 5.0)).thenReturn(lonLe);

		spec.toPredicate(root, query, cb);

		verify(cb).greaterThanOrEqualTo(latitudePath, 40.0);
		verify(cb).lessThanOrEqualTo(latitudePath, 50.0);
		verify(cb).greaterThanOrEqualTo(longitudePath, -5.0);
		verify(cb).lessThanOrEqualTo(longitudePath, 5.0);
	}

	@SuppressWarnings("unchecked")
	@Test
	void findMemoriesOnMapWithCriteria_worldWideBox_shouldNormalizeToFullWorld() {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setNorth(90.0);
		criteria.setSouth(-90.0);
		criteria.setWest(-200.0);
		criteria.setEast(200.0);

		ArgumentCaptor<Specification<Memory>> specCaptor = ArgumentCaptor.forClass(specificationClass());

		when(memoryRepository.findAll(specCaptor.capture())).thenReturn(List.of());

		when(userService.getCurrentUser()).thenReturn(null);

		memoryService.findMemoriesOnMapWithCriteria(criteria);

		Specification<Memory> spec = specCaptor.getValue();
		assertNotNull(spec);

		@SuppressWarnings("rawtypes")
		Root root = mock(Root.class);
		CriteriaQuery<?> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);

		@SuppressWarnings("rawtypes")
		Path locationPath = mock(Path.class);
		@SuppressWarnings("rawtypes")
		Path latitudePath = mock(Path.class);
		@SuppressWarnings("rawtypes")
		Path longitudePath = mock(Path.class);

		lenient().when(root.get("location")).thenReturn(locationPath);
		lenient().when(locationPath.get("latitude")).thenReturn(latitudePath);
		lenient().when(locationPath.get("longitude")).thenReturn(longitudePath);

		Predicate latGe = mock(Predicate.class);
		Predicate latLe = mock(Predicate.class);
		Predicate lonGe = mock(Predicate.class);
		Predicate lonLe = mock(Predicate.class);
		Predicate finalAnd = mock(Predicate.class);

		when(cb.greaterThanOrEqualTo(latitudePath, -90.0)).thenReturn(latGe);
		when(cb.lessThanOrEqualTo(latitudePath, 90.0)).thenReturn(latLe);

		// ATTENTION : on vérifie bien -180 / 180
		when(cb.greaterThanOrEqualTo(longitudePath, -180.0)).thenReturn(lonGe);
		when(cb.lessThanOrEqualTo(longitudePath, 180.0)).thenReturn(lonLe);

		when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

		spec.toPredicate(root, query, cb);

		verify(cb).greaterThanOrEqualTo(longitudePath, -180.0);
		verify(cb).lessThanOrEqualTo(longitudePath, 180.0);
	}

	@SuppressWarnings("unchecked")
	@Test
	void findMemoriesOnMapWithCriteria_crossAntiMeridian_shouldUseOrForLongitude() {
		SearchCriteria criteria = new SearchCriteria();
		criteria.setNorth(10.0);
		criteria.setSouth(-10.0);
		criteria.setWest(170.0);
		criteria.setEast(-170.0);

		ArgumentCaptor<Specification<Memory>> specCaptor = ArgumentCaptor.forClass(specificationClass());

		when(memoryRepository.findAll(specCaptor.capture())).thenReturn(List.of());

		when(userService.getCurrentUser()).thenReturn(null);

		memoryService.findMemoriesOnMapWithCriteria(criteria);

		Specification<Memory> spec = specCaptor.getValue();
		assertNotNull(spec);

		@SuppressWarnings("rawtypes")
		Root root = mock(Root.class);
		CriteriaQuery<?> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);

		@SuppressWarnings("rawtypes")
		Path locationPath = mock(Path.class);
		@SuppressWarnings("rawtypes")
		Path latitudePath = mock(Path.class);
		@SuppressWarnings("rawtypes")
		Path longitudePath = mock(Path.class);

		lenient().when(root.get("location")).thenReturn(locationPath);
		lenient().when(locationPath.get("latitude")).thenReturn(latitudePath);
		lenient().when(locationPath.get("longitude")).thenReturn(longitudePath);

		Predicate latGe = mock(Predicate.class);
		Predicate latLe = mock(Predicate.class);
		Predicate lonGe = mock(Predicate.class);
		Predicate lonLe = mock(Predicate.class);
		Predicate orPredicate = mock(Predicate.class);
		Predicate finalAnd = mock(Predicate.class);

		when(cb.greaterThanOrEqualTo(latitudePath, -10.0)).thenReturn(latGe);
		when(cb.lessThanOrEqualTo(latitudePath, 10.0)).thenReturn(latLe);

		when(cb.greaterThanOrEqualTo(longitudePath, 170.0)).thenReturn(lonGe);
		when(cb.lessThanOrEqualTo(longitudePath, -170.0)).thenReturn(lonLe);

		when(cb.or(any(Predicate[].class))).thenReturn(orPredicate);
		when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

		spec.toPredicate(root, query, cb);

		verify(cb).greaterThanOrEqualTo(longitudePath, 170.0);
		verify(cb).lessThanOrEqualTo(longitudePath, -170.0);
		verify(cb).or(any(Predicate[].class));
	}

//  Tests de getMemoryById

	@Test
	void getMemoryById_whenMemoryExists_shouldReturnIt() {
		Long id = 8L;
		User user = makeUser(1L);
		Location location = makeLocation(10L);
		Memory memory = makeMemory(id, user, location);
		memory.setState(MemoryState.PUBLISHED);

		when(memoryRepository.findById(id)).thenReturn(Optional.of(memory));

		when(userService.getCurrentUser()).thenReturn(new User());

		Memory result = memoryService.getMemoryById(id);

		assertNotNull(result);
		assertEquals(id, result.getMemoryId());
		verify(memoryRepository).findById(id);
	}

	@Test
	void getMemoryById_whenMemoryDoesNotExist_shouldThrowEntityNotFoundException() {
		Long id = 42L;
		when(memoryRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> memoryService.getMemoryById(id));

		verify(memoryRepository).findById(id);
	}

//  Tests de getMemoryForModification

	@Test
	void getMemoryForModification_shouldReturnMemory_whenMemoryExistsAndUserAuthorized() {
		long id = 1L;

		User owner = makeUser(10L);
		Location location = makeLocation(10L);
		Memory memory = makeMemory(id, owner, location);

		memory.setState(MemoryState.PUBLISHED);

		when(memoryRepository.findById(id)).thenReturn(Optional.of(memory));

		when(userService.getCurrentUser()).thenReturn(owner);
		when(userService.isAdmin()).thenReturn(false);

		Memory result = memoryService.getMemoryForModification(id);

		assertSame(memory, result);
		verify(memoryRepository).findById(id);
	}

	@Test
	void getMemoryForModification_shouldThrowEntityNotFound_whenMemoryDoesNotExist() {
		long id = 42L;

		when(memoryRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> memoryService.getMemoryForModification(id));

		verify(memoryRepository).findById(id);
	}

	@Test
	void getMemoryForModification_shouldThrowUnauthorized_whenUserNotAuthorized() {
		long id = 5L;

		User owner = makeUser(10L);
		User stranger = makeUser(99L);
		Location location = makeLocation(10L);
		Memory memory = makeMemory(id, owner, location);

		memory.setVisibility(MemoryVisibility.PRIVATE);
		memory.setState(MemoryState.PUBLISHED);

		when(memoryRepository.findById(id)).thenReturn(Optional.of(memory));

		when(userService.getCurrentUser()).thenReturn(stranger);
		when(userService.isAdmin()).thenReturn(false);

		assertThrows(UnauthorizedActionException.class, () -> memoryService.getMemoryForModification(id));
	}

//  Tests de getMemoryByImage

	@Test
	void getMemoryByImage_shouldReturnMemory_whenUUIDExists() {
		String uuid = "abc-123";

		Memory memory = new Memory();
		when(memoryRepository.findByMediaUUID(uuid)).thenReturn(memory);

		Memory result = memoryService.getMemoryByImage(uuid);

		assertSame(memory, result);
		verify(memoryRepository).findByMediaUUID(uuid);
	}

	@Test
	void getMemoryByImage_shouldReturnNull_whenUUIDNotFound() {
		String uuid = "does-not-exist";

		when(memoryRepository.findByMediaUUID(uuid)).thenReturn(null);

		Memory result = memoryService.getMemoryByImage(uuid);

		assertNull(result);
		verify(memoryRepository).findByMediaUUID(uuid);
	}

	@Test
    void getMemoryByImage_shouldReturnNull_whenUUIDIsNull() {
        when(memoryRepository.findByMediaUUID(null)).thenReturn(null);

        Memory result = memoryService.getMemoryByImage(null);

        assertNull(result);
        verify(memoryRepository).findByMediaUUID(null);
    }

//  Tests de createMemory

	@Test
	void createMemory_shouldThrowUnauthorized_whenUserNotLoggedIn() {
		Memory memory = new Memory();

		when(userService.getCurrentUser()).thenReturn(null);

		UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class,
				() -> memoryService.createMemory(memory, imageFile, true, new Location()));
		assertEquals("Vous devez vous connecter pour ajouter un souvenir", ex.getMessage());
		verify(locationService, never()).saveLocation(any());
	}

	@Test
	void createMemory_shouldCreateMemoryCorrectly() {
		User user = makeUser(1L);
		Location location = makeLocation(1L);
		Memory memory = new Memory();

		when(userService.getCurrentUser()).thenReturn(user);

		Location savedLocation = makeLocation(8L);
		when(locationService.saveLocation(location)).thenReturn(savedLocation);

		when(memoryRepository.save(memory)).thenReturn(memory);

		Memory result = memoryService.createMemory(memory, null, true, location);

		assertNotNull(result);
		assertEquals(savedLocation, result.getLocation());
		assertEquals(user, result.getRememberer());
		assertNotNull(result.getCreationDate());

		verify(userService).getCurrentUser();
		verify(locationService).saveLocation(location);
		verify(memoryRepository).save(memory);
	}

	@Test
	void createMemory_shouldApplyUnpublishedState() {
		Location location = makeLocation(1L);
		Memory memory = new Memory();
		when(userService.getCurrentUser()).thenReturn(new User());
		when(locationService.saveLocation(location)).thenReturn(location);
		when(memoryRepository.save(memory)).thenReturn(memory);

		memoryService.createMemory(memory, null, false, location);

		assertEquals(MemoryState.CREATED, memory.getState());
	}

//  Tests de updateMemory

	@Test
    void updateMemory_shouldThrow_whenMemoryDoesNotExist() {
        when(memoryRepository.findById(10L)).thenReturn(Optional.empty());

        Memory updated = new Memory();
        updated.setMemoryId(10L);

        assertThrows(EntityNotFoundException.class,
                () -> memoryService.updateMemory(updated, null, false, new Location()));

        verify(userService, never()).getCurrentUser();
        verify(memoryRepository, never()).save(any());
        verify(locationService, never()).saveLocation(any());
    }

	@Test
	void updateMemory_shouldCallAssertUserModify_andPass() {
		long memoryId = 1L;
		long userId = 5L;
		User user = makeUser(userId);

		Location existingLocation = makeLocation(1L);
		existingLocation.setName("Paris");

		Location locationWithUpdate = makeLocation(1L);
		locationWithUpdate.setName("Paris");

		Memory existing = makeMemory(memoryId, user, existingLocation);

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);

		when(locationService.getById(memoryId)).thenReturn(existingLocation);

		when(memoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		Memory result = memoryService.updateMemory(updated, null, false, locationWithUpdate);

		assertNotNull(result);

		verify(userService).getCurrentUser();
		verify(memoryRepository).save(existing);
	}

	@Test
	void updateMemory_shouldUpdateBasicFields() {
		long memoryId = 1L;
		long userId = 5L;

		User user = new User();
		user.setUserId(userId);

		Category existingCategory = makeCategory(1L);
		Location existingLocation = makeLocation(1L);
		existingLocation.setName("Paris");

		Memory existing = makeMemory(memoryId, user, existingLocation);
		existing.setTitle("old title");
		existing.setDescription("old desc");
		existing.setCategory(existingCategory);

		Category newCategory = makeCategory(5L);
		Memory updated = new Memory();
		updated.setMemoryId(memoryId);
		updated.setTitle("new title");
		updated.setDescription("new desc");
		updated.setMemoryDate(LocalDate.now().minusDays(1));
		updated.setCategory(newCategory);
		updated.setVisibility(MemoryVisibility.MEMBERS);

		Location locationWithUpdate = makeLocation(8L);
		locationWithUpdate.setName("Nantes");

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);

		when(locationService.getById(memoryId)).thenReturn(existingLocation);

		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Memory result = memoryService.updateMemory(updated, null, false, locationWithUpdate);

		assertEquals("new title", result.getTitle());
		assertEquals("new desc", result.getDescription());
		assertEquals(newCategory, result.getCategory());
		assertEquals(MemoryVisibility.MEMBERS, result.getVisibility());
	}

	@Test
	void updateMemory_shouldSetStatePublished_whenPublishTrue() {
		long memoryId = 1L;
		long userId = 5L;
		long locationId = 8L;
		User user = makeUser(userId);
		Location location = makeLocation(locationId);
		Memory existing = makeMemory(memoryId, user, location);

		Memory updated = new Memory();
		updated.setMemoryId(1L);

		when(memoryRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(updated, null, true, location);

		assertEquals(MemoryState.PUBLISHED, result.getState());
	}

	@Test
	void updateMemory_shouldSetStateCreated_whenPublishFalse() {
		long memoryId = 1L;
		long userId = 5L;
		long locationId = 8L;
		User user = makeUser(userId);
		Location location = makeLocation(locationId);
		Memory existing = makeMemory(memoryId, user, location);

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(updated, null, false, location);

		assertEquals(MemoryState.CREATED, result.getState());
	}

	@Test
	void updateMemory_shouldNotChangeMedia_whenImageNull() {
		long memoryId = 1L;
		long userId = 5L;
		long locationId = 8L;
		User user = makeUser(userId);
		Location location = makeLocation(locationId);
		Memory existing = makeMemory(memoryId, user, location);
		existing.setMediaUUID("uuid123");

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		when(memoryRepository.findById(userId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(updated, null, false, location);

		assertEquals("uuid123", result.getMediaUUID());
		verify(fileService, never()).deleteFile(any());
		verify(fileService, never()).saveFile(any());
	}

	@Test
	void updateMemory_shouldReplaceMedia_whenNewImageProvided() throws Exception {
		long memoryId = 1L;
		long userId = 5L;
		long locationId = 8L;
		User user = makeUser(userId);
		Location location = makeLocation(locationId);
		Memory existing = makeMemory(memoryId, user, location);
		existing.setMediaUUID("olduuid");

		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(fileService.saveFile(file)).thenReturn("newuuid");

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(updated, file, false, location);

		verify(fileService).deleteFile("olduuid");
		verify(fileService).saveFile(file);
		assertEquals("newuuid", result.getMediaUUID());
	}

	@Test
	void updateMemory_shouldKeepOldMedia_whenFileStorageExceptionOccurs() throws Exception {
		long memoryId = 1L;
		long userId = 5L;
		long locationId = 8L;
		User user = makeUser(userId);
		Location location = makeLocation(locationId);
		Memory existing = makeMemory(memoryId, user, location);
		existing.setMediaUUID("olduuid");

		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);

		when(fileService.saveFile(file)).thenThrow(new FileStorageException("err"));

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(updated, file, false, location);

		assertEquals("olduuid", result.getMediaUUID());
		verify(fileService).deleteFile("olduuid");
	}

	@Test
	void updateMemory_shouldUpdateExistingLocation_whenOnlyOneMemory() {
		long memoryId = 1L;
		long userId = 5L;
		long currentLocationId = 8L;
		User user = makeUser(userId);
		Location currentLocation = makeLocation(currentLocationId);
		Memory existing = makeMemory(memoryId, user, currentLocation);

		Location updatedLocation = new Location();
		updatedLocation.setName("new name");

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);

		when(locationService.getById(currentLocationId)).thenReturn(currentLocation);
		when(locationService.saveLocation(any())).thenAnswer(i -> i.getArgument(0));

		when(memoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		Memory result = memoryService.updateMemory(updated, null, false, updatedLocation);

		verify(locationService).saveLocation(updatedLocation);
		assertEquals(currentLocationId, updatedLocation.getLocationId());
//		assertEquals("new name", result.getLocation().getName().toString());
	}
}
