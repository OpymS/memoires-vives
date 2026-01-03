package fr.memoires_vives.bll;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.FileStorageException;
import fr.memoires_vives.exception.UnauthorizedActionException;

@Service
public class FileServiceImpl implements FileService {
	@Value("${upload.path}")
	private String uploadPath;

	public FileServiceImpl() {
	}

	@Override
	public String saveFile(MultipartFile file) {
		if (file == null) {
			return "";
		}
		String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

		try {
			saveWithName(file, uniqueFileName);
		} catch (IllegalStateException | IOException e) {
			throw new FileStorageException("Impossible d'enregistrer le fichier " + file.getOriginalFilename(), e);
		}
		return uniqueFileName;
	}

	@Override
	public String saveUserFile(MultipartFile file, String pseudo) {
		if (file == null) {
			return "";
		}

		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
		}
		String uniqueFileName = UUID.randomUUID().toString() + "_" + pseudo + extension;

		try {
			saveWithName(file, uniqueFileName);
		} catch (IOException e) {
			throw new FileStorageException("Impossible d'enregistrer le fichier " + originalFilename, e);
		}
		return uniqueFileName;
	}

	@Override
	public boolean deleteFile(String mediaUUID) {
		if (mediaUUID == null || mediaUUID.trim().isEmpty()) {
			return false;
		}

		Path filePath;
		try {
			filePath = Paths.get(uploadPath, mediaUUID);
		} catch (InvalidPathException | NullPointerException e) {
			throw new FileStorageException("Chemin de fichier invalide pour : " + mediaUUID, e);
		}
		if (!Files.exists(filePath)) {
			return false;
		}
		try {
			Files.delete(filePath);
			return true;
		} catch (IOException e) {
			throw new FileStorageException("Impossible de supprimer le fichier " + filePath, e);
		}
	}

	public void deleteUserFile(User user, String mediaUUID, Memory memory) {

		boolean isOwner = (user.getMediaUUID() != null && user.getMediaUUID().equals(mediaUUID))
				|| (memory.getRememberer().getUserId() == user.getUserId());

		if (!isOwner) {
			throw new UnauthorizedActionException("Vous n'avez pas le droit de supprimer cette image");
		}

		boolean deleted = deleteFile(mediaUUID);
		if (!deleted) {
			throw new EntityNotFoundException("Image non trouvée.");
		}
	}

	public Resource getFileResource(String filename) {
		try {
			Path filePath = Paths.get(uploadPath).resolve(filename);
			Resource resource = new UrlResource(filePath.toUri());

			if (!resource.exists() || !resource.isReadable()) {
				throw new EntityNotFoundException("Fichier introuvable");
			}

			return resource;
		} catch (MalformedURLException e) {
			throw new FileStorageException("Chemin de fichier invalide : " + filename, e);
		}
	}

	private void saveWithName(MultipartFile file, String name) throws IOException {
		Path uploadDir = Paths.get(uploadPath);
		if (!Files.exists(uploadDir)) {
			try {
				Files.createDirectories(uploadDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Path filePath = uploadDir.resolve(name);
		Files.write(filePath, file.getBytes());
	}

}
