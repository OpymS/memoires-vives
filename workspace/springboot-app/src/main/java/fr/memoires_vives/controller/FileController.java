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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/uploads")
public class FileController {
	@Value("${upload.path}")
	private String uploadPath;
	
	@GetMapping("/{filename:.+}")
	public ResponseEntity<Resource> getFile(@PathVariable("filename") String filename) throws MalformedURLException  {
		Path filePath = Paths.get(uploadPath).resolve(filename);
		Resource resource = new UrlResource(filePath.toUri());
		
		if (!resource.exists() || !resource.isReadable()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable");
		}
		
		return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_JPEG)
				.body(resource);
	}
	
}
