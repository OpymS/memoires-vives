package fr.memoires_vives.bll;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.Memory;

@Service
public class MemoryServiceMock implements MemoryService {

	@Override
	public List<Memory> findMemories() {
		List<Memory> memories = new ArrayList<Memory>();
		Memory memory1 = new Memory();
		memory1.setMemoryId(0);
		memory1.setTitle("Souvenir 1");
		memory1.setDescription("C'est le premier souvenir d'une longue liste");
		memories.add(memory1);
		Memory memory2 = new Memory();
		memory2.setMemoryId(1);
		memory2.setTitle("Souvenir 2");
		memory2.setDescription("C'est un autre souvenir");
		memories.add(memory2);
		Memory memory3 = new Memory();
		memory3.setMemoryId(2);
		memory3.setTitle("Souvenir 3");
		memory3.setDescription("encore un autre");
		memories.add(memory3);
		return memories;
	}

}
