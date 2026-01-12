package fr.memoires_vives.controller;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.repositories.MemoryRepository;

@RestController
public class SitemapController {
	@Value("${app.base-url}")
	private String baseUrl;

	private final MemoryRepository memoryRepository;

	public SitemapController(MemoryRepository memoryRepository) {
		this.memoryRepository = memoryRepository;
	}

	@GetMapping(value = "/sitemap.xml", produces = "application/xml")
	public String sitemap() {

		StringBuilder xml = new StringBuilder(10_000);

		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

		xml.append("""
				    <url>
				        <loc>%s/</loc>
				        <changefreq>daily</changefreq>
				        <priority>1.0</priority>
				    </url>
				""".formatted(baseUrl));

		memoryRepository.findAllByState(MemoryState.PUBLISHED).forEach(memory -> {

			LocalDate lastMod = memory.getModificationDate() != null ? memory.getModificationDate().toLocalDate()
					: memory.getCreationDate().toLocalDate();

			xml.append("""
					    <url>
					        <loc>%s/memory/%d-%s</loc>
					        <lastmod>%s</lastmod>
					        <changefreq>monthly</changefreq>
					        <priority>0.8</priority>
					    </url>
					""".formatted(baseUrl, memory.getMemoryId(), memory.getSlug(), lastMod));
		});

		xml.append("</urlset>");

		return xml.toString();
	}
}
