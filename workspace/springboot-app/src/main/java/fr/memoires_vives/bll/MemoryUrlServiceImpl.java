package fr.memoires_vives.bll;

import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;

@Service
public class MemoryUrlServiceImpl implements MemoryUrlService {

	@Override
	public String buildCanonicalUrl(Memory memory) {
		Location location = memory.getLocation();

		StringBuilder url = new StringBuilder("/memory");

		if (location != null && location.getCountrySlug() != null) {
			url.append("/").append(location.getCountrySlug());

			if (location.getCitySlug() != null) {
				url.append("/").append(location.getCitySlug());
			}
		}

		url.append("/")
			.append(memory.getMemoryId())
			.append("-")
			.append(memory.getSlug());

		return url.toString();
	}

}
