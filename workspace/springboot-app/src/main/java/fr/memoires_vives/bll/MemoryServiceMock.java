package fr.memoires_vives.bll;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;

@Service
//@Primary
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
		Memory memory4 = new Memory();
		memory4.setMemoryId(3);
		memory4.setTitle("Souvenir 4");
		memory4.setDescription("C'est un mauvais souvenir");
		memories.add(memory4);
		Memory memory5 = new Memory();
		memory5.setMemoryId(4);
		memory5.setTitle("Souvenir 5");
		memory5.setDescription("C'est un bon souvenir");
		memories.add(memory5);
		Memory memory6 = new Memory();
		memory6.setMemoryId(5);
		memory6.setTitle("Souvenir 6");
		memory6.setDescription("encore un très bon !");
		memories.add(memory6);
		return memories;
	}

	@Override
	public void createMemory(Memory memory, MultipartFile image, Boolean publish, Location location) {
		// TODO Auto-generated method stub
		
	}

}
