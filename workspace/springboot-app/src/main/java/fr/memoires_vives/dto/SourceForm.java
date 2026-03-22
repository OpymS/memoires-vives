package fr.memoires_vives.dto;

import org.hibernate.validator.constraints.URL;

import fr.memoires_vives.bo.SourceStatus;
import fr.memoires_vives.utils.UrlUtil;
import jakarta.validation.constraints.NotBlank;

public class SourceForm {

	@NotBlank
	@URL(message = "L'URL n'est pas valide")
	private String url;
	
	private String domain;

	private SourceStatus status;

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
	 * @return the domain
	 */
	public String getDomain() {
		if (url == null || url.isBlank()) {
			return null;
		}
		return UrlUtil.extractDomain(url);
	}

	/**
	 * @return the status
	 */
	public SourceStatus getStatus() {
		return status;
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

	/**
	 * @param status the status to set
	 */
	public void setStatus(SourceStatus status) {
		this.status = status;
	}

	public String getStatusClass() {
		if (status == null)
			return "bg-gray-100 text-gray-800";

		return switch (status) {
		case APPROVED -> "bg-green-100 text-green-800";
		case PENDING -> "bg-orange-100 text-orange-800";
		case REJECTED -> "bg-red-100 text-red-800";
		};
	}

}
