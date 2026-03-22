package fr.memoires_vives.bll;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.Source;
import fr.memoires_vives.bo.SourceStatus;
import fr.memoires_vives.exception.InvalidSourceUrlException;
import fr.memoires_vives.repositories.SourceRepository;

@Service
public class SourceServiceImpl implements SourceService {
	private final SourceRepository sourceRepository;

	public SourceServiceImpl(SourceRepository sourceRepository) {
		this.sourceRepository = sourceRepository;
	}

	@Override
	public Source createSource(Memory memory, String url) {
		URI uri = validateUrl(url);

		String normalizedUrl = normalizeUrl(uri);
		String domain = uri.getHost();

		Source source = new Source();
		source.setMemory(memory);
		source.setUrl(normalizedUrl);
		source.setDomain(domain);
		source.setStatus(SourceStatus.PENDING);
		source.setCreatedAt(LocalDateTime.now());

		int score = computeCredibilityScore(normalizedUrl, domain);
		source.setCredibilityScore(score);

		return source;
	}

	@Override
	public boolean alreadyExists(Memory memory, String url) {
		URI uri = validateUrl(url);
		String normalizedUrl = normalizeUrl(uri);
		return memory.getSources().stream().anyMatch(s -> normalizedUrl.equals(s.getUrl()));
	}

	@Override
	public List<Source> findAll() {
		return sourceRepository.findAll();
	}

	@Override
	public List<Source> findByStatus(SourceStatus status) {
		return sourceRepository.findByStatus(status);
	}

	@Override
	@Transactional
	public void updateStatus(Long id, SourceStatus status) {
		sourceRepository.updateStatus(id, status);
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
				throw new InvalidSourceUrlException("Seules les URLs HTTP ou HTTPS sont autorisées");
			}

			if (uri.getHost() == null) {
				throw new InvalidSourceUrlException("URL invalide");
			}

			return uri;
		} catch (URISyntaxException e) {
			throw new InvalidSourceUrlException("URL invalide");
		}
	}

	private String normalizeUrl(URI uri) {

		String url = uri.normalize().toString();

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		return url;
	}

}
