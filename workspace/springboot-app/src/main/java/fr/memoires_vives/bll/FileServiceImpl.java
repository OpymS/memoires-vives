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
		
		Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        if (file == null) {
        	return "";
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        Path filePath = uploadDir.resolve(uniqueFileName);
        Files.write(filePath, file.getBytes());
        
        return filePath.toString();
	}

}
