package fr.memoires_vives.controller;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.memoires_vives.bll.LocationService;
import fr.memoires_vives.bll.MemoryUrlService;
import fr.memoires_vives.bo.MemoryState;
import fr.memoires_vives.repositories.MemoryRepository;

@RestController
public class SitemapController {
	@Value("${app.base-url}")
	private String baseUrl;

	private final MemoryUrlService memoryUrlService;
	private final LocationService locationService;

	private final MemoryRepository memoryRepository;
	

	public SitemapController(MemoryRepository memoryRepository, MemoryUrlService memoryUrlService, LocationService locationService) {
		this.locationService = locationService;
		this.memoryRepository = memoryRepository;
		this.memoryUrlService = memoryUrlService;
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
			String canonicalUrl = baseUrl + memoryUrlService.buildCanonicalUrl(memory);

			LocalDate lastMod = memory.getModificationDate() != null ? memory.getModificationDate().toLocalDate()
					: memory.getCreationDate().toLocalDate();

			xml.append("""
					    <url>
					        <loc>%s</loc>
					        <lastmod>%s</lastmod>
					        <changefreq>monthly</changefreq>
					        <priority>0.8</priority>
					    </url>
					""".formatted(canonicalUrl, lastMod));
		});
		
		locationService.getCountrySlugs().forEach(countrySlug -> {
			String countryUrl = baseUrl + "/memories/" + countrySlug;
			xml.append("""
					    <url>
					        <loc>%s</loc>
					        <changefreq>weekly</changefreq>
					        <priority>0.7</priority>
					    </url>
					""".formatted(countryUrl));
			
			locationService.getCitySlugsByCountry(countrySlug).forEach(citySlug -> {
				String cityUrl = countryUrl + "/" + citySlug;
				xml.append("""
					    <url>
					        <loc>%s</loc>
					        <changefreq>weekly</changefreq>
					        <priority>0.6</priority>
					    </url>
					""".formatted(cityUrl));
				
			});
		});

		xml.append("</urlset>");

		return xml.toString();
	}
}
