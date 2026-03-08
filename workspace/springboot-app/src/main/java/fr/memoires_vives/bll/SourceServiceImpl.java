package fr.memoires_vives.bll;

import java.net.URI;
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
	public Source addSource(Memory memory, String url) {
		if (sourceRepository.findByMemory_MemoryIdAndUrl(memory.getMemoryId(), url).isPresent()) {
			throw new IllegalArgumentException("Cette source existe déjà pour cette mémoire");
		}

		String domain = extractDomain(url);

		Source source = new Source();
		source.setMemory(memory);
		source.setUrl(url);
		source.setDomain(domain);
		source.setStatus(SourceStatus.PENDING);
		source.setCreatedAt(LocalDateTime.now());

		int score = computeCredibilityScore(url, domain);
		source.setCredibilityScore(score);

		return sourceRepository.save(source);
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
}
