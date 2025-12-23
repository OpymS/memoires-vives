package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.FileStorageException;
import fr.memoires_vives.exception.UnauthorizedActionException;

class FileServiceImplTest {

	private FileServiceImpl fileService;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		fileService = new FileServiceImpl();
		ReflectionTestUtils.setField(fileService, "uploadPath", tempDir.toString());
	}

//  Tests de saveFile

	@Test
	void saveFile_shouldReturnEmptyString_whenFileIsNull() {
		String result = fileService.saveFile(null);
		assertEquals("", result);
	}

	@Test
	void saveFile_shouldSaveFileAndReturnGeneratedName() {
		MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

		String filename = fileService.saveFile(file);

		assertNotNull(filename);
		assertTrue(filename.endsWith("_test.txt"));
		assertTrue(Files.exists(tempDir.resolve(filename)));
	}

	@Test
	void saveFile_shouldThrowFileStorageException_whenIOExceptionOccurs() {
		MultipartFile file = spy(new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes()));

		try {
			doThrow(IOException.class).when(file).getBytes();
		} catch (IOException ignored) {
		}

		FileStorageException exception = assertThrows(FileStorageException.class, () -> fileService.saveFile(file));

		assertTrue(exception.getMessage().contains("Impossible d'enregistrer le fichier"));
	}

//  Tests de saveUserFile

	@Test
	void saveUserFile_shouldReturnEmptyString_whenFileIsNull() {
		String result = fileService.saveUserFile(null, "pseudo");
		assertEquals("", result);
	}

	@Test
	void saveUserFile_shouldPreserveExtensionAndIncludePseudo() {
		MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());

		String filename = fileService.saveUserFile(file, "john");

		assertTrue(filename.contains("_john.png"));
		assertTrue(Files.exists(tempDir.resolve(filename)));
	}

	@Test
	void saveUserFile_shouldHandleFileWithoutExtension() {
		MultipartFile file = new MockMultipartFile("file", "avatar", "image/png", "img".getBytes());

		String filename = fileService.saveUserFile(file, "john");

		assertTrue(filename.contains("_john"));
		assertFalse(filename.endsWith("."));
	}

//  Tests de deleteFile

	@Test
	void deleteFile_shouldReturnFalse_whenUuidIsNullOrBlank() {
		assertFalse(fileService.deleteFile(null));
		assertFalse(fileService.deleteFile(" "));
	}

	@Test
	void deleteFile_shouldReturnFalse_whenFileDoesNotExist() {
		assertFalse(fileService.deleteFile("unknown.txt"));
	}

	@Test
	void deleteFile_shouldDeleteFileAndReturnTrue() throws IOException {
		Path file = tempDir.resolve("file.txt");
		Files.write(file, "data".getBytes());

		boolean result = fileService.deleteFile("file.txt");

		assertTrue(result);
		assertFalse(Files.exists(file));
	}

//  Tests de deleteUserFile

	@Test
	void deleteUserFile_shouldThrowUnauthorizedException_whenNotOwner() {
		User user = mock(User.class);
		User rememberer = mock(User.class);
		Memory memory = mock(Memory.class);

		when(user.getUserId()).thenReturn(1L);
		when(user.getMediaUUID()).thenReturn("other-media");

		when(memory.getRememberer()).thenReturn(rememberer);
		when(rememberer.getUserId()).thenReturn(2L);

		assertThrows(UnauthorizedActionException.class, () -> fileService.deleteUserFile(user, "media", memory));
	}

	@Test
	void deleteUserFile_shouldDelete_whenUserIsRememberer() throws IOException {
		Path file = tempDir.resolve("media");
		Files.write(file, "data".getBytes());

		User user = mock(User.class);
		User rememberer = mock(User.class);
		Memory memory = mock(Memory.class);

		when(user.getUserId()).thenReturn(1L);
		when(user.getMediaUUID()).thenReturn(null);

		when(memory.getRememberer()).thenReturn(rememberer);
		when(rememberer.getUserId()).thenReturn(1L);

		boolean result = fileService.deleteFile("media");

		assertTrue(result);
	}

	@Test
	void deleteUserFile_shouldDelete_whenUserOwnsMediaUUID() throws IOException {
		Path file = tempDir.resolve("media");
		Files.write(file, "data".getBytes());

		User user = mock(User.class);
		User rememberer = mock(User.class);
		Memory memory = mock(Memory.class);

		when(user.getUserId()).thenReturn(1L);
		when(user.getMediaUUID()).thenReturn("media");

		when(memory.getRememberer()).thenReturn(rememberer);
		when(rememberer.getUserId()).thenReturn(999L);

		fileService.deleteUserFile(user, "media", memory);

		assertFalse(Files.exists(file));
	}

//  Tests de getFileResource

	@Test
	void getFileResource_shouldReturnResource_whenFileExists() throws IOException {
		Path file = tempDir.resolve("file.txt");
		Files.write(file, "data".getBytes());

		Resource resource = fileService.getFileResource("file.txt");

		assertNotNull(resource);
		assertTrue(resource.exists());
		assertTrue(resource.isReadable());
	}

	@Test
	void getFileResource_shouldThrowEntityNotFoundException_whenFileDoesNotExist() {
		assertThrows(EntityNotFoundException.class, () -> fileService.getFileResource("missing.txt"));
	}
}
