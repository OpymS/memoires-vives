package fr.memoires_vives.bll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.exception.FileStorageException;

@Service
public class FileServiceImpl implements FileService {
	@Value("${upload.path}")
	private String uploadPath;

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

	private void saveWithName(MultipartFile file, String name) throws IOException{
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

	@Override
	public boolean deleteFile(String mediaUUID) {
		Path filePath = Paths.get(uploadPath, mediaUUID);
		if (Files.exists(filePath)) {
			try {
				Files.delete(filePath);
			} catch (IOException e) {
				throw new FileStorageException("Impossible de supprimer le fichier " + filePath, e);
			}
			return true;
		}
		return false;
	}

}
