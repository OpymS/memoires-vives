package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.UnauthorizedActionException;
import fr.memoires_vives.repositories.MemoryRepository;

@ExtendWith(MockitoExtension.class)
public class MemoryServiceImplCategoryTest {

	@Mock
	private MemoryRepository memoryRepository;

	@Mock
	private UserService userService;

	@Mock
	private CategoryService categoryService;

	@InjectMocks
	private MemoryServiceImpl memoryService;

//  Tests de getMemoriesByCategory

	@Test
	void getMemoriesByCategory_shouldReturnListFromRepository() {
		long categoryId = 42L;

		Category category = new Category();
		category.setCategoryId(categoryId);

		List<Memory> expected = Arrays.asList(new Memory(), new Memory());

		when(memoryRepository.findByCategoryId(categoryId)).thenReturn(expected);

		List<Memory> result = memoryService.getMemoriesByCategory(category);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertSame(expected, result);

		verify(memoryRepository).findByCategoryId(categoryId);
		verifyNoMoreInteractions(memoryRepository);
	}

// Tests de getMemoriesByCategoryForAdmin

	@Test
	void getMemoriesByCategoryForAdmin_shouldThrowUnauthorized_whenUserIsNotAdmin() {
		long categoryId = 1L;

		when(userService.isAdmin()).thenReturn(false);

		UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class,
				() -> memoryService.getMemoriesByCategoryForAdmin(categoryId));

		assertEquals("Access forbidden", ex.getMessage());
		verify(userService).isAdmin();
		verifyNoMoreInteractions(userService, categoryService, memoryRepository);
	}

	@Test
	void getMemoriesByCategoryForAdmin_shouldThrowNotFound_whenCategoryDoesNotExist() {
		long categoryId = 1L;

		when(userService.isAdmin()).thenReturn(true);
		when(categoryService.getCategoryById(categoryId)).thenReturn(null);

		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
				() -> memoryService.getMemoriesByCategoryForAdmin(categoryId));

		assertEquals("Category not found", ex.getMessage());
		verify(userService).isAdmin();
		verify(categoryService).getCategoryById(categoryId);
		verifyNoMoreInteractions(userService, categoryService, memoryRepository);
	}

	@Test
	void getMemoriesByCategoryForAdmin_shouldReturnList_whenAllValid() {
		long categoryId = 1L;

		Category category = new Category();
		category.setCategoryId(categoryId);

		List<Memory> expected = Arrays.asList(new Memory(), new Memory());

		when(userService.isAdmin()).thenReturn(true);
		when(categoryService.getCategoryById(categoryId)).thenReturn(category);
		when(memoryRepository.findByCategoryId(categoryId)).thenReturn(expected);

		List<Memory> result = memoryService.getMemoriesByCategoryForAdmin(categoryId);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertSame(expected, result);

		verify(userService).isAdmin();
		verify(categoryService).getCategoryById(categoryId);
		verify(memoryRepository).findByCategoryId(categoryId);
		verifyNoMoreInteractions(userService, categoryService, memoryRepository);
	}
}
