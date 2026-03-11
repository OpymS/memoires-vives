package fr.memoires_vives.bll;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.Source;
import fr.memoires_vives.bo.SourceStatus;
import fr.memoires_vives.repositories.SourceRepository;

@Service
public class SourceServiceImpl implements SourceService {
	private final SourceRepository sourceRepository;

	public SourceServiceImpl(SourceRepository sourceRepository) {
		this.sourceRepository = sourceRepository;
	}

	@Override
	public Source createSource(Memory memory, String url) {
		String domain = extractDomain(url);

		Source source = new Source();
		source.setMemory(memory);
		source.setUrl(url);
		source.setDomain(domain);
		source.setStatus(SourceStatus.PENDING);
		source.setCreatedAt(LocalDateTime.now());

		int score = computeCredibilityScore(url, domain);
		source.setCredibilityScore(score);

		return source;
	}

	@Override
	public boolean alreadyExists(Memory memory, String url) {
		return memory.getSources().stream().anyMatch(s -> s.getUrl().equals(url));
	}

	private String extractDomain(String url) {
		try {
			URI uri = new URI(url);
			String host = uri.getHost();
			return host.startsWith("www.") ? host.substring(4) : host;
		} catch (Exception e) {
			throw new IllegalArgumentException("URL invalide");
		}
	}

	private int computeCredibilityScore(String url, String domain) {

		int score = 50;

		if (url.startsWith("https"))
			score += 10;

		if (url.length() > 300)
			score -= 10;

		if (domain.endsWith(".edu") || domain.endsWith(".ac.uk"))
			score += 30;

		if (domain.endsWith(".gov") || domain.endsWith(".gouv.fr"))
			score += 40;

		return score;
	}

	private URI validateUrl(String url) {
		try {
			URI uri = new URI(url);
			String scheme = uri.getScheme();

			if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
				throw new IllegalArgumentException("Protocole non autorisé");
			}

			return uri;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("URL invalide");
		}
	}
}
