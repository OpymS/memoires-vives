package fr.memoires_vives.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtil {

	public static String extractDomain(String url) {
		if (url == null || url.isBlank()) {
			return null;
		}

		try {
			URI uri = new URI(url);
			return uri.getHost();
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
