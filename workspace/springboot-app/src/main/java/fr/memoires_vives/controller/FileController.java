package fr.memoires_vives.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

	private FileService fileService;
	private UserService userService;
	private MemoryService memoryService;

	public FileController(FileService fileService, UserService userService, MemoryService memoryService) {
		this.fileService = fileService;
		this.userService = userService;
		this.memoryService = memoryService;
	}

	@GetMapping("/{filename:.+}")
	public ResponseEntity<Resource> getFile(@PathVariable("filename") String filename) throws MalformedURLException {
		Path filePath = Paths.get(uploadPath).resolve(filename);
		Resource resource = new UrlResource(filePath.toUri());

		if (!resource.exists() || !resource.isReadable()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable");
		}

		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
	}

	@DeleteMapping("/images/uploadedImages/{uuid}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> deleteImage(@PathVariable String uuid) {
		User currentUser = userService.getCurrentUser();
		Memory memoryWithUUID = memoryService.getMemoryByImage(uuid);
		if (currentUser == null || (currentUser.getMediaUUID() != uuid
				&& currentUser.getUserId() != memoryWithUUID.getRememberer().getUserId())) {
			return ResponseEntity.status(403).body("Vous n'avez pas le droit de supprimer cette image");
		}
		try {
			boolean isDeleted = fileService.deleteFile(uuid);
			if (isDeleted) {
				return ResponseEntity.ok("Image supprimée avec succès");
			} else {
				return ResponseEntity.status(404).body("Image non trouvée");
			}
		} catch (FileStorageException e) {
			return ResponseEntity.status(500).body("Erreur lors de la suppression de l'image.");
		}
	}
}
