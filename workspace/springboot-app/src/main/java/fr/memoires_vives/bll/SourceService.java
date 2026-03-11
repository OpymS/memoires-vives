package fr.memoires_vives.bll;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.Source;

public interface SourceService {
	Source createSource(Memory memory, String url);
	boolean alreadyExists(Memory memory, String url);
}
