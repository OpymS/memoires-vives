package fr.memoires_vives.bll;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.repositories.MemoryRepository;

@Primary
@Service
public class MemoryServiceImpl implements MemoryService {
	
	private final MemoryRepository memoryRepository;
	
	public MemoryServiceImpl(MemoryRepository memoryRepository) {
		this.memoryRepository = memoryRepository;
	}

	@Override
	public List<Memory> findMemories() {
		return memoryRepository.findAll();
	}

}
