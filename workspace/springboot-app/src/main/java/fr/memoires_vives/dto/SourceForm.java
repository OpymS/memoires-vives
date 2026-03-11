package fr.memoires_vives.dto;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;

public class SourceForm {

	@NotBlank
	@URL(message = "L'URL n'est pas valide")
	private String url;
	private String domain;

	public SourceForm() {
	}

	public SourceForm(String url, String domain) {
		this.url = url;
		this.domain = domain;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
}
