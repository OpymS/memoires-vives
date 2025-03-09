package fr.memoires_vives.bll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {
	@Value("${upload.path}")
	private String uploadPath;

	@Override
	public String saveFile(MultipartFile file) throws IOException {
		if (file == null) {
			return "";
		}
		String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

		saveWithName(file, uniqueFileName);
		return uniqueFileName;
	}

	@Override
	public String saveUserFile(MultipartFile file, String pseudo) throws IOException {
		if (file == null) {
			return "";
		}

		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
		}
		String uniqueFileName = UUID.randomUUID().toString() + "_" + pseudo + extension;

		saveWithName(file, uniqueFileName);
		return uniqueFileName;
	}

	private void saveWithName(MultipartFile file, String name) throws IOException {
		Path uploadDir = Paths.get(uploadPath);
		if (!Files.exists(uploadDir)) {
			Files.createDirectories(uploadDir);
		}
		Path filePath = uploadDir.resolve(name);
		Files.write(filePath, file.getBytes());
	}

}
