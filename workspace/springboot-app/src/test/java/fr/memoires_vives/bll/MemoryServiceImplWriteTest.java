package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import fr.memoires_vives.dto.MemoryForm;
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
	private CategoryService categoryService;

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
		m.setMemoryDate(LocalDate.now().minusYears(3L));
		return m;
	}

//  Tests de createMemory

	@Test
	void createMemory_shouldThrowUnauthorized_whenUserNotLoggedIn() {
		User user = makeUser(3L);
		Location location = makeLocation(8L);
		Memory memory = makeMemory(1L, user, location);
		MemoryForm form = MemoryForm.fromMemoryEntity(memory);

		when(userService.getCurrentUser()).thenReturn(null);

		UnauthorizedActionException ex = assertThrows(UnauthorizedActionException.class,
				() -> memoryService.createMemory(form, imageFile));
		assertEquals("Vous devez vous connecter pour ajouter un souvenir", ex.getMessage());
		verify(locationService, never()).saveLocation(any());
	}

	@Test
	void createMemory_shouldCreateMemoryCorrectly() {
		User user = makeUser(1L);
		Location location = makeLocation(8L);
		Memory memory = makeMemory(2L, user, location);
		memory.setMemoryDate(LocalDate.now().minusYears(3L));
		MemoryForm form = MemoryForm.fromMemoryEntity(memory);

		when(userService.getCurrentUser()).thenReturn(user);

		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(location);

		when(locationService.saveLocation(any(Location.class))).thenReturn(location);

		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
		when(memoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		Memory result = memoryService.createMemory(form, null);

		assertNotNull(result);
		assertEquals(location, result.getLocation());
		assertEquals(user, result.getRememberer());
		assertNotNull(result.getCreationDate());

		verify(userService).getCurrentUser();
		verify(locationService).createFromCoordinates(anyDouble(), anyDouble());
		verify(locationService).saveLocation(any(Location.class));
		verify(memoryRepository).save(any());
	}

	@Test
	void createMemory_shouldApplyUnpublishedState() {
		Location location = makeLocation(8L);
		User user = makeUser(1L);
		Memory memory = makeMemory(2L, user, location);
		memory.setState(MemoryState.CREATED);

		MemoryForm form = MemoryForm.fromMemoryEntity(memory);

		when(userService.getCurrentUser()).thenReturn(user);
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(location);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
		when(memoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		Memory result = memoryService.createMemory(form, null);

		assertEquals(MemoryState.CREATED, result.getState());
	}

//  Tests de updateMemory

	@Test
	void updateMemory_shouldThrow_whenMemoryDoesNotExist() {
		Location location = makeLocation(8L);
		User user = makeUser(1L);
		Memory updated = makeMemory(2L, user, location);
		updated.setMemoryId(10L);

		when(memoryRepository.findById(10L)).thenReturn(Optional.empty());

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		assertThrows(EntityNotFoundException.class, () -> memoryService.updateMemory(form, null, false));

		verify(userService, never()).getCurrentUser();
		verify(memoryRepository, never()).save(any());
		verify(locationService, never()).saveLocation(any());
	}

	@Test
	void updateMemory_shouldCallAssertUserModify_andPass() {
		User user = makeUser(1L);

		Location existingLocation = makeLocation(1L);
		existingLocation.setName("Paris");

		Location locationWithUpdate = makeLocation(1L);
		locationWithUpdate.setName("Paris");

		Memory existing = makeMemory(2L, user, existingLocation);

		Memory updated = makeMemory(2L, user, locationWithUpdate);

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		when(memoryRepository.findById(2L)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(existingLocation);
		when(locationService.getById(1L)).thenReturn(existingLocation);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));

		when(memoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		Memory result = memoryService.updateMemory(form, null, false);

		assertNotNull(result);

		verify(userService).getCurrentUser();
		verify(memoryRepository).save(existing);
	}

	@Test
	void updateMemory_shouldUpdateBasicFields() {
		long memoryId = 1L;
		long userId = 5L;

		User user = makeUser(userId);

		Category existingCategory = makeCategory(1L);
		Location existingLocation = makeLocation(1L);
		existingLocation.setName("Paris");

		Memory existing = makeMemory(memoryId, user, existingLocation);
		existing.setTitle("old title");
		existing.setDescription("old desc");
		existing.setCategory(existingCategory);

		Category newCategory = makeCategory(5L);

		Location locationWithUpdate = makeLocation(8L);
		locationWithUpdate.setName("Nantes");

		Memory updated = makeMemory(memoryId, user, locationWithUpdate);
		updated.setTitle("new title");
		updated.setDescription("new desc");
		updated.setCategory(newCategory);

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);

		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(existingLocation);
		when(locationService.getById(anyLong())).thenReturn(existingLocation);

		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(newCategory));
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Memory result = memoryService.updateMemory(form, null, false);

		assertEquals("new title", result.getTitle());
		assertEquals("new desc", result.getDescription());
		assertEquals(newCategory, result.getCategory());
	}

	@Test
	void updateMemory_shouldSetStatePublished_whenPublishTrue() {
		long memoryId = 1L;
		long userId = 5L;
		long locationId = 8L;
		User user = makeUser(userId);
		Location location = makeLocation(locationId);
		Memory existing = makeMemory(memoryId, user, location);

		Memory updated = makeMemory(memoryId, user, location);
		updated.setState(MemoryState.PUBLISHED);

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		when(memoryRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(location);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(form, null, false);

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
		existing.setState(MemoryState.PUBLISHED);

		Memory updated = makeMemory(memoryId, user, location);
		updated.setState(MemoryState.CREATED);

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(location);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(form, null, false);

		assertEquals(MemoryState.CREATED, result.getState());
	}

	@Test
	void updateMemory_shouldNotChangeMedia_whenImageNullAndImageNotDeleted() {
		long memoryId = 1L;
		long userId = 5L;
		long locationId = 8L;
		User user = makeUser(userId);
		Location location = makeLocation(locationId);
		Memory existing = makeMemory(memoryId, user, location);
		existing.setMediaUUID("uuid123");

		Memory updated = makeMemory(memoryId, user, location);

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(location);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(form, null, false);

		assertEquals("uuid123", result.getMediaUUID());
		verify(fileService, never()).deleteFile(any());
		verify(fileService, never()).saveFile(any());
	}

	// TODO test dans le cas où l'utilisateur supprime l'image

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

		Memory updated = makeMemory(memoryId, user, location);
		updated.setMediaUUID("olduuid");

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(location);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(form, file, false);

		verify(fileService).deleteFile("olduuid");
		verify(fileService).saveFile(file);
		assertEquals("newuuid", result.getMediaUUID());
	}

	// TODO : tester le cas où on veut supprimer l'image

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

		Memory updated = makeMemory(memoryId, user, location);

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(location);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
		when(memoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(locationService.getById(locationId)).thenReturn(location);

		Memory result = memoryService.updateMemory(form, file, false);

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

		Location updatedLocation = makeLocation(currentLocationId);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(currentLocation);
		when(locationService.getById(currentLocationId)).thenReturn(currentLocation);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
		when(memoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(locationService.saveLocation(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Memory updated = makeMemory(memoryId, user, updatedLocation);
		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);

		Memory result = memoryService.updateMemory(form, null, false);

		verify(locationService).saveLocation(locationCaptor.capture());

		verify(locationService).saveLocation(currentLocation);
		assertEquals(currentLocationId, result.getLocation().getLocationId());
	}

	
	// TODO à refaire, ne teste rien
//	@Test
//	void updateMemory_shouldCreateNewLocation_whenDifferentAndMultipleMemories() {
//		long memoryId = 1L;
//		long userId = 5L;
//		long currentLocationId = 8L;
//		User user = makeUser(userId);
//		Location currentLocation = makeLocation(currentLocationId);
//
//		Memory existing = makeMemory(memoryId, user, currentLocation);
//		currentLocation.getMemories().add(existing);
//		currentLocation.getMemories().add(new Memory()); // >1
//
//		Location updatedLocation = new Location();
//		updatedLocation.setName("different");
//		updatedLocation.setLatitude(10.0);
//		updatedLocation.setLongitude(20.0);
//
//		Location savedNewLoc = makeLocation(99L);
//		savedNewLoc.setName("different");
//
//		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
//		when(userService.getCurrentUser()).thenReturn(user);
//		when(userService.isAdmin()).thenReturn(false);
//
//		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(updatedLocation);
//		when(locationService.getById(currentLocationId)).thenReturn(updatedLocation);
//		when(locationService.saveLocation(updatedLocation)).thenReturn(savedNewLoc);
//		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
//		when(memoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//		Memory updated = makeMemory(memoryId, user, updatedLocation);
//
//		MemoryForm form = MemoryForm.fromMemoryEntity(updated);
//
//		Memory result = memoryService.updateMemory(form, null, false);
//
//		assertEquals(savedNewLoc, result.getLocation());
//	}

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
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(currentLocation);
		when(locationService.getById(currentLocationId)).thenReturn(currentLocation);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));
		
		when(memoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		Memory updated = makeMemory(memoryId, user, updatedLocation);

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		Memory result = memoryService.updateMemory(form, null, false);

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

		Memory updated = makeMemory(memoryId, user, currentLocation);

		when(memoryRepository.findById(memoryId)).thenReturn(Optional.of(existing));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.isAdmin()).thenReturn(false);
		when(locationService.createFromCoordinates(anyDouble(), anyDouble())).thenReturn(currentLocation);
		when(locationService.getById(currentLocationId)).thenReturn(currentLocation);
		when(categoryService.getCategoryById(anyLong())).thenReturn(Optional.of(makeCategory(1L)));

		when(memoryRepository.save(existing)).thenThrow(new DataAccessException("db") {
		});

		Location loc = makeLocation(1L);

		updated.setLocation(loc);

		MemoryForm form = MemoryForm.fromMemoryEntity(updated);

		assertThrows(DataPersistenceException.class, () -> memoryService.updateMemory(form, null, false));
	}
}
