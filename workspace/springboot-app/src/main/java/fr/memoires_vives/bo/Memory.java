package fr.memoires_vives.bo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "memories")
public class Memory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long memoryId;

	@Column(nullable = false)
	@NotBlank
	@Size(max = 30)
	private String title;

	@Column(nullable = false)
	private String slug;

	@Column(nullable = false)
	private String description;

	private String mediaUUID;

	@Column(nullable = false)
	@PastOrPresent(message = "Le souvenir ne peut pas être dans le futur.")
	private LocalDate memoryDate;

	@Column(nullable = false, updatable = false)
	private LocalDateTime creationDate;

	private LocalDateTime modificationDate;

	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = true)
	@NotNull
	private Category category;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnore
	private User rememberer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "VARCHAR(50)")
	private MemoryState state;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "VARCHAR(50)")
	private MemoryVisibility visibility;

	@ManyToMany
	@JoinTable(name = "memory_group", joinColumns = @JoinColumn(name = "memory_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
	@JsonIgnore
	private List<Group> groups;

	public Memory() {
	}

	/**
	 * @return the memoryId
	 */
	public long getMemoryId() {
		return memoryId;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the slug
	 */
	public String getSlug() {
		return slug;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the mediaUUID
	 */
	public String getMediaUUID() {
		return mediaUUID;
	}

	/**
	 * @return the memoryDate
	 */
	public LocalDate getMemoryDate() {
		return memoryDate;
	}

	/**
	 * @return the creationDate
	 */
	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	/**
	 * @return the modificationDate
	 */
	public LocalDateTime getModificationDate() {
		return modificationDate;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @return the category
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * @return the rememberer
	 */
	public User getRememberer() {
		return rememberer;
	}

	/**
	 * @return the state
	 */
	public MemoryState getState() {
		return state;
	}

	/**
	 * @return the visibility
	 */
	public MemoryVisibility getVisibility() {
		return visibility;
	}

	/**
	 * @return the groups
	 */
	public List<Group> getGroups() {
		return groups;
	}

	/**
	 * @param memoryId the memoryId to set
	 */
	public void setMemoryId(long memoryId) {
		this.memoryId = memoryId;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param slug the slug to set
	 */
	public void setSlug(String slug) {
		this.slug = slug;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param mediaUUID the mediaUUID to set
	 */
	public void setMediaUUID(String mediaUUID) {
		this.mediaUUID = mediaUUID;
	}

	/**
	 * @param memoryDate the memoryDate to set
	 */
	public void setMemoryDate(LocalDate memoryDate) {
		this.memoryDate = memoryDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @param modificationDate the modificationDate to set
	 */
	public void setModificationDate(LocalDateTime modificationDate) {
		this.modificationDate = modificationDate;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(Category category) {
		this.category = category;
	}

	/**
	 * @param rememberer the rememberer to set
	 */
	public void setRememberer(User rememberer) {
		this.rememberer = rememberer;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(MemoryState state) {
		this.state = state;
	}

	/**
	 * @param visibility the visibility to set
	 */
	public void setVisibility(MemoryVisibility visibility) {
		this.visibility = visibility;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public String toStringWithCollections() {
		StringBuilder builder = new StringBuilder();
		builder.append("Memory [memoryId=");
		builder.append(memoryId);
		builder.append(", title=");
		builder.append(title);
		builder.append(", description=");
		builder.append(description);
		builder.append(", mediaUUID=");
		builder.append(mediaUUID);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", modificationDate=");
		builder.append(modificationDate);
		builder.append(", location=");
		builder.append(location);
		builder.append(", rememberer=");
		builder.append(rememberer);
		builder.append(", state=");
		builder.append(state);
		builder.append(", visibility=");
		builder.append(visibility);
		builder.append(", groups=");
		builder.append(groups);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Memory [memoryId=");
		builder.append(memoryId);
		builder.append(", title=");
		builder.append(title);
		builder.append(", description=");
		builder.append(description);
		builder.append(", mediaUUID=");
		builder.append(mediaUUID);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", modificationDate=");
		builder.append(modificationDate);
		builder.append(", location=");
		builder.append(location);
		builder.append(", category=");
		builder.append(category);
		builder.append(", rememberer=");
		builder.append(rememberer.getUserId());
		builder.append(", state=");
		builder.append(state);
		builder.append(", visibility=");
		builder.append(visibility);
		builder.append("]");
		return builder.toString();
	}
}
