package fr.memoires_vives.bll;

import java.util.List;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.Source;
import fr.memoires_vives.bo.SourceStatus;

public interface SourceService {
	Source createSource(Memory memory, String url);
	boolean alreadyExists(Memory memory, String url);
	List<Source> findAll();
	List<Source> findByStatus(SourceStatus status);
	void updateStatus(Long id, SourceStatus status);
}
