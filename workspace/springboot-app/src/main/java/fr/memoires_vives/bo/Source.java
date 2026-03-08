package fr.memoires_vives.bo;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.URL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "sources")
public class Source {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sourceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memory_id", nullable = false)
	private Memory memory;

	@URL
	@Size(max = 2048)
	@Column(nullable = false, length = 2048)
	private String url;

	@Column(nullable = false, length = 255)
	private String domain;

	@Column(length = 512)
	private String title;

	@Column(name = "credibility_score")
	private Integer credibilityScore;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SourceStatus status;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	/**
	 * @return the sourceId
	 */
	public Long getSourceId() {
		return sourceId;
	}

	/**
	 * @return the memory
	 */
	public Memory getMemory() {
		return memory;
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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the credibilityScore
	 */
	public Integer getCredibilityScore() {
		return credibilityScore;
	}

	/**
	 * @return the status
	 */
	public SourceStatus getStatus() {
		return status;
	}

	/**
	 * @return the createdAt
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param sourceId the sourceId to set
	 */
	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * @param memory the memory to set
	 */
	public void setMemory(Memory memory) {
		this.memory = memory;
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
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param credibilityScore the credibilityScore to set
	 */
	public void setCredibilityScore(Integer credibilityScore) {
		this.credibilityScore = credibilityScore;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(SourceStatus status) {
		this.status = status;
	}

	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
