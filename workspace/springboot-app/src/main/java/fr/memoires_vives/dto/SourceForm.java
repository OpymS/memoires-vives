package fr.memoires_vives.dto;

import org.hibernate.validator.constraints.URL;

import fr.memoires_vives.utils.UrlUtil;
import jakarta.validation.constraints.NotBlank;

public class SourceForm {

	@NotBlank
	@URL(message = "L'URL n'est pas valide")
	private String url;

	public SourceForm() {
	}

	public SourceForm(String url) {
		this.url = url;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		if (url == null || url.isBlank()) {
			return null;
		}

		return UrlUtil.extractDomain(url);
	}
}
