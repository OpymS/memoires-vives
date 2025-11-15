package fr.memoires_vives.bll;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.User;

public interface FileService {
	String saveFile(MultipartFile file);
	String saveUserFile(MultipartFile file, String pseudo);
	void deleteUserFile(User user, String mediaUUID, Memory memory); 
	boolean deleteFile(String mediaUUID);
	Resource getFileResource(String filename);
}
