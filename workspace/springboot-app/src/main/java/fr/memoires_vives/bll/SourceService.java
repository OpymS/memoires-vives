package fr.memoires_vives.bll;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.Source;

public interface SourceService {
	Source addSource(Memory memory, String url);
	
}
