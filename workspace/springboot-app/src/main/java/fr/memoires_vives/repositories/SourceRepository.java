package fr.memoires_vives.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.memoires_vives.bo.Source;
import fr.memoires_vives.bo.SourceStatus;

public interface SourceRepository extends JpaRepository<Source, Long> {
	List<Source> findByMemory_MemoryId(Long memoryId);
	
	Optional<Source> findByMemory_MemoryIdAndUrl(Long memoryId, String url);
	
	List<Source> findByStatus(SourceStatus status);

}
