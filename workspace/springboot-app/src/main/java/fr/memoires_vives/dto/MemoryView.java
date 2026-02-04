package fr.memoires_vives.dto;

import fr.memoires_vives.bo.Memory;

public record MemoryView(Memory memory, String canonicalUrl) {

}
