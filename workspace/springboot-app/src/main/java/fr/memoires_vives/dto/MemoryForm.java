package fr.memoires_vives.dto;

import java.time.LocalDate;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.MemoryState;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public class MemoryForm {

	private Long memoryId = 0L;

	@NotBlank
	@Size(max = 30)
	private String title;

	@NotBlank
	private String description;

	@PastOrPresent(message = "Le souvenir ne peut pas être dans le futur.")
	@NotNull
	private LocalDate memoryDate;

	@NotNull
	private Long categoryId;

	private String mediaUUID;

	private Double latitude;

	private Double longitude;

	@AssertTrue(message = "Vous devez choisir un lieu sur la carte")
	private Boolean locationSelected = false;

	private Boolean published;

	/**
	 * @return the memoryId
	 */
	public Long getMemoryId() {
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
	 * @return the memoryDate
	 */
	public LocalDate getMemoryDate() {
		return memoryDate;
	}

	/**
	 * @return the categoryId
	 */
	public Long getCategoryId() {
		return categoryId;
	}

	/**
	 * @return the mediaUUID
	 */
	public String getMediaUUID() {
		return mediaUUID;
	}

	/**
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * @return the locationSelected
	 */
	public Boolean getLocationSelected() {
		return locationSelected;
	}

	/**
	 * @return the published
	 */
	public Boolean getPublished() {
		return published;
	}

	/**
	 * @param memoryId the memoryId to set
	 */
	public void setMemoryId(Long memoryId) {
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
	 * @param memoryDate the memoryDate to set
	 */
	public void setMemoryDate(LocalDate memoryDate) {
		this.memoryDate = memoryDate;
	}

	/**
	 * @param categoryId the categoryId to set
	 */
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * @param mediaUUID the mediaUUID to set
	 */
	public void setMediaUUID(String mediaUUID) {
		this.mediaUUID = mediaUUID;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @param locationSelected the locationSelected to set
	 */
	public void setLocationSelected(Boolean locationSelected) {
		this.locationSelected = locationSelected;
	}

	/**
	 * @param published the published to set
	 */
	public void setPublished(Boolean published) {
		this.published = published;
	}

	public static MemoryForm fromMemoryEntity(Memory memory) {
		MemoryForm form = new MemoryForm();
		form.setMemoryId(memory.getMemoryId());
		form.setTitle(memory.getTitle());
		form.setDescription(memory.getDescription());
		form.setMemoryDate(memory.getMemoryDate());
		form.setCategoryId(memory.getCategory().getCategoryId());
		form.setMediaUUID(memory.getMediaUUID());
		Location location = memory.getLocation();
		if (location != null) {
			form.setLatitude(location.getLatitude().doubleValue());
			form.setLongitude(location.getLongitude().doubleValue());
			form.setLocationSelected(true);
		}
		form.setPublished(memory.getState() == MemoryState.PUBLISHED ? true : false);

		return form;
	}
}
