package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.dto.SearchCriteria;
import fr.memoires_vives.repositories.MemoryRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
public class MemoryServiceImplSearchTest {

	@Mock
	private MemoryRepository memoryRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private MemoryServiceImpl memoryService;

	@SuppressWarnings("unchecked")
	private static Class<Specification<Memory>> specificationClass() {
		return (Class<Specification<Memory>>) (Class<?>) Specification.class;
	}

	private List<Memory> makeMemories(int count) {
		List<Memory> memories = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			memories.add(new Memory());
		}
		return memories;
	}

	private User makeUser(long id) {
		User u = new User();
		u.setUserId(id);
		return u;
	}

//  Tests de findMemories

	@Test
	void findMemories_shouldReturnFirstPage() {
		List<Memory> allMemories = makeMemories(10);
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
		List<Memory> allMemories = makeMemories(10);
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
		List<Memory> allMemories = makeMemories(10);
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
		List<Memory> allMemories = makeMemories(10);
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
		List<Memory> allMemories = makeMemories(6);
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
}
