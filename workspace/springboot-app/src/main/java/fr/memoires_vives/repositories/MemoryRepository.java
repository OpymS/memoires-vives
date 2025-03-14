package fr.memoires_vives.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.memoires_vives.bo.Memory;

public interface MemoryRepository extends JpaRepository<Memory, Long> {

	List<Memory> findByTitle(String title);
	Memory findByMemoryId(long memoryId);
	Memory findByMediaUUID(String mediaUUID);
}
