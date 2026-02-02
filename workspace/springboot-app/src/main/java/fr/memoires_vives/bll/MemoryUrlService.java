package fr.memoires_vives.bll;

import fr.memoires_vives.bo.Memory;

public interface MemoryUrlService {
	String buildCanonicalUrl(Memory memory);
}
