package fr.memoires_vives.bo;

import java.time.LocalDateTime;

public class Memory {
	private int memoryId;
	private String title;
	private String description;
	private String mediaUUID;
	private LocalDateTime memoryDate;
	private LocalDateTime creationDate;
	private LocalDateTime modificationDate;
	private Location location;
	private User rememberer;
	
	public Memory() {
	}

	/**
	 * @return the memoryId
	 */
	public int getMemoryId() {
		return memoryId;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
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
	public LocalDateTime getMemoryDate() {
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
	 * @return the rememberer
	 */
	public User getRememberer() {
		return rememberer;
	}

	/**
	 * @param memoryId the memoryId to set
	 */
	public void setMemoryId(int memoryId) {
		this.memoryId = memoryId;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
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
	public void setMemoryDate(LocalDateTime memoryDate) {
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
	 * @param rememberer the rememberer to set
	 */
	public void setRememberer(User rememberer) {
		this.rememberer = rememberer;
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
		builder.append(", memoryDate=");
		builder.append(memoryDate);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", modificationDate=");
		builder.append(modificationDate);
		builder.append("]");
		return builder.toString();
	}
	
}
