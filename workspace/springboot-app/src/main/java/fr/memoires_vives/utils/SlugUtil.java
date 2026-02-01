package fr.memoires_vives.utils;

import java.text.Normalizer;
import java.util.Locale;

import fr.memoires_vives.bo.Memory;

public class SlugUtil {

	public static String toSlug(String input) {
		if (input == null || input.isBlank()) {
			return null;
		}

		String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

		return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // supprime les accents
				.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s-]", "") // caractères non ASCII
				.replaceAll("\\s+", "-") // remplace espaces par tirets
				.replaceAll("-+", "-") // retire tirets multiples
				.replaceAll("^-|-$", ""); // trim tirets
	}

	public static String toSlug(Memory memory) {
		String base = memory.getTitle() + "-" + memory.getMemoryDate().getYear();
		return toSlug(base);
//		String title = memory.getTitle();
//		int year = memory.getMemoryDate().getYear();
//		String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
//				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "") + "-" + year; // supprime les accents
//		return normalized.toLowerCase().replaceAll("[^a-z0-9\\s-]", "") // garde lettres/chiffres/espaces
//				.replaceAll("\\s+", "-") // remplace espaces par tirets
//				.replaceAll("-+", "-"); // retire tirets multiples
	}

}
