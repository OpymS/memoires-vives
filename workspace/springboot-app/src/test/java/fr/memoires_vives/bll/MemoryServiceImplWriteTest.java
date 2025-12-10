package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Category;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.bo.MemoryVisibility;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.DataPersistenceException;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.FileStorageException;
import fr.memoires_vives.exception.UnauthorizedActionException;
import fr.memoires_vives.repositories.MemoryRepository;

@ExtendWith(MockitoExtension.class)
public class MemoryServiceImplWriteTest {

	@Mock
	private MemoryRepository memoryRepository;

	@Mock
	private FileService fileService;

	@Mock
	private LocationService locationService;

	@Mock
	private UserService userService;

	@Mock
	private MultipartFile imageFile;

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

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
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
		when(locationService.saveLocation(any())).thenAnswer(invocation -> invocation.getArgument(0));

		when(memoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		Memory result = memoryService.updateMemory(updated, null, false, updatedLocation);

		verify(locationService).saveLocation(updatedLocation);
		assertEquals(currentLocationId, updatedLocation.getLocationId());
		assertEquals("new name", result.getLocation().getName().toString());
	}

	@Test
	void updateMemory_shouldCreateNewLocation_whenDifferentAndMultipleMemories() {
		long memoryId = 1L;
		long userId = 5L;
		long currentLocationId = 8L;
		User user = makeUser(userId);
		Location currentLocation = makeLocation(currentLocationId);

		Memory existing = makeMemory(memoryId, user, currentLocation);
		currentLocation.getMemories().add(existing);
		currentLocation.getMemories().add(new Memory()); // >1

		Location updatedLocation = new Location();
		updatedLocation.setName("different");
		updatedLocation.setLatitude(10.0);
		updatedLocation.setLongitude(20.0);

		Location savedNewLoc = makeLocation(99L);
		savedNewLoc.setName("different");

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);

		when(locationService.getById(currentLocationId)).thenReturn(currentLocation);
		when(locationService.saveLocation(updatedLocation)).thenReturn(savedNewLoc);

		when(memoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		Memory result = memoryService.updateMemory(updated, null, false, updatedLocation);

		assertEquals(savedNewLoc, result.getLocation());
	}

	@Test
	void updateMemory_shouldNotChangeLocation_whenSameValues() {
		long memoryId = 1L;
		long userId = 5L;
		long currentLocationId = 8L;
		User user = makeUser(userId);
		Location currentLocation = makeLocation(currentLocationId);
		currentLocation.setName("same");
		currentLocation.setLatitude(1.0);
		currentLocation.setLongitude(2.0);
		Memory existing = makeMemory(memoryId, user, currentLocation);
		currentLocation.getMemories().add(existing);
		currentLocation.getMemories().add(new Memory()); // >1

		Location updatedLocation = new Location();
		updatedLocation.setName("same");
		updatedLocation.setLatitude(1.0);
		updatedLocation.setLongitude(2.0);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);

		when(locationService.getById(currentLocationId)).thenReturn(currentLocation);

		when(memoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		Memory result = memoryService.updateMemory(updated, null, false, updatedLocation);

		assertEquals(currentLocation, result.getLocation());
		verify(locationService, never()).saveLocation(any());
	}

	@SuppressWarnings("serial")
	@Test
	void updateMemory_shouldThrowDataPersistence_whenSaveFails() {
		long memoryId = 1L;
		long userId = 5L;
		long currentLocationId = 8L;
		User user = makeUser(userId);
		Location currentLocation = makeLocation(currentLocationId);
		Memory existing = makeMemory(memoryId, user, currentLocation);

		Memory updated = new Memory();
		updated.setMemoryId(memoryId);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);

		when(locationService.getById(currentLocationId)).thenReturn(currentLocation);

		when(memoryRepository.save(existing)).thenThrow(new DataAccessException("db") {
		});

		Location loc = makeLocation(1L);

		assertThrows(DataPersistenceException.class, () -> memoryService.updateMemory(updated, null, false, loc));
	}
}
