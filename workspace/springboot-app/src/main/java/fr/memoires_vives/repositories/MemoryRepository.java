package fr.memoires_vives.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;

public interface MemoryRepository extends JpaRepository<Memory, Long>, JpaSpecificationExecutor<Memory> {

	List<Memory> findByTitle(String title);
	Optional<Memory> findById(long memoryId);
	Memory findByMediaUUID(String mediaUUID);
	List<Memory> findAllByState(MemoryState state);
	
	@Query("SELECT m FROM Memory m WHERE m.category.categoryId = :categoryId")
	List<Memory> findByCategoryId(@Param("categoryId") long categoryId);
}
