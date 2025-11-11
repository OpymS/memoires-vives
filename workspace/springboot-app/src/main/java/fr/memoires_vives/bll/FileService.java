package fr.memoires_vives.bll;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	String saveFile(MultipartFile file);
	String saveUserFile(MultipartFile file, String pseudo);
	boolean deleteFile(String mediaUUID);
}
