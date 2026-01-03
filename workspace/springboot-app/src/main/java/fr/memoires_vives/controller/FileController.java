package fr.memoires_vives.controller;

import java.net.MalformedURLException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fr.memoires_vives.bll.FileService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.FileStorageException;

@RestController
@RequestMapping("/uploads")
public class FileController {
	@Value("${upload.path}")
	private String uploadPath;

	private final FileService fileService;
	private final UserService userService;
	private final MemoryService memoryService;

	public FileController(FileService fileService, UserService userService, MemoryService memoryService) {
		this.fileService = fileService;
		this.userService = userService;
		this.memoryService = memoryService;
	}

	@GetMapping("/{filename:.+}")
	public ResponseEntity<Resource> getFile(@PathVariable("filename") String filename) throws MalformedURLException {
		try {
			Resource resource = fileService.getFileResource(filename);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
		} catch (FileStorageException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable");
		}
	}

	@DeleteMapping("/images/uploadedImages/{uuid}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> deleteImage(@PathVariable("uuid") String uuid) {
		User currentUser = userService.getCurrentUser();
		Memory memory = memoryService.getMemoryByImage(uuid);
		fileService.deleteUserFile(currentUser, uuid, memory);
		return ResponseEntity.ok("Image supprimée avec succès.");
	}
}
