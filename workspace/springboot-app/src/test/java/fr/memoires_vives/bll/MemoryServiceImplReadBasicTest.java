package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.bo.MemoryVisibility;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.UnauthorizedActionException;
import fr.memoires_vives.repositories.MemoryRepository;

@ExtendWith(MockitoExtension.class)
public class MemoryServiceImplReadBasicTest {

	@Mock
	private MemoryRepository memoryRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private MemoryServiceImpl memoryService;

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
}