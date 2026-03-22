package fr.memoires_vives.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.memoires_vives.bo.Source;
import fr.memoires_vives.bo.SourceStatus;

public interface SourceRepository extends JpaRepository<Source, Long> {
	List<Source> findByMemory_MemoryId(Long memoryId);
	
	Optional<Source> findByMemory_MemoryIdAndUrl(Long memoryId, String url);
	
	List<Source> findByStatus(SourceStatus status);
	
	@Modifying
	@Query("UPDATE Source s SET s.status = :status WHERE s.id = :id")
	void updateStatus(@Param("id") Long id, @Param("status") SourceStatus status);

}
