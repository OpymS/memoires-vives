package fr.memoires_vives.bll;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	String saveFile(MultipartFile file) throws IOException;
}
