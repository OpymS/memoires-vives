package fr.memoires_vives.utils;

import java.text.Normalizer;

import fr.memoires_vives.bo.Memory;

public class SlugUtil {
	public static String toSlug(Memory memory) {
		String title = memory.getTitle();
		int year = memory.getMemoryDate().getYear();
		String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "") + "-" + year; 	// supprime les accents
		return normalized.toLowerCase().replaceAll("[^a-z0-9\\s-]", "") 			// garde lettres/chiffres/espaces
				.replaceAll("\\s+", "-") 											// remplace espaces par tirets
				.replaceAll("-+", "-"); 											// retire tirets multiples
	}

}
