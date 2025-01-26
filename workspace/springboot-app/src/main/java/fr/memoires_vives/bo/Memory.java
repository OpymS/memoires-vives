package fr.memoires_vives.bo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "memories")
public class Memory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long memoryId;
	
	@Column(nullable = false)
	private String title;
	
	@Column(nullable = false)
	private String description;
	
	private String mediaUUID;
	
	@Column(nullable = false)
	private LocalDate memoryDate;
	private int memoryYear;
	private int memoryMonth;
	private int memoryDay;
	private int memoryHour;
	private int memoryMinute;
	
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime modificationDate;
	
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User rememberer;
	
	@Column(nullable = false)
	private MemoryState state;
	
	@Column(nullable = false)
	private MemoryVisibility visibility;
	
	@ManyToMany
	@JoinTable(
		name = "memory_group",
		joinColumns = @JoinColumn(name = "memory_id"),
		inverseJoinColumns = @JoinColumn(name = "group_id")
	)
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
	 * @return the memoryYear
	 */
	public int getMemoryYear() {
		return memoryYear;
	}

	/**
	 * @return the memoryMonth
	 */
	public int getMemoryMonth() {
		return memoryMonth;
	}

	/**
	 * @return the memoryDay
	 */
	public int getMemoryDay() {
		return memoryDay;
	}

	/**
	 * @return the memoryHour
	 */
	public int getMemoryHour() {
		return memoryHour;
	}

	/**
	 * @return the memoryMinute
	 */
	public int getMemoryMinute() {
		return memoryMinute;
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
	 * @param memoryYear the memoryYear to set
	 */
	public void setMemoryYear(int memoryYear) {
		this.memoryYear = memoryYear;
	}

	/**
	 * @param memoryMonth the memoryMonth to set
	 */
	public void setMemoryMonth(int memoryMonth) {
		this.memoryMonth = memoryMonth;
	}

	/**
	 * @param memoryDay the memoryDay to set
	 */
	public void setMemoryDay(int memoryDay) {
		this.memoryDay = memoryDay;
	}

	/**
	 * @param memoryHour the memoryHour to set
	 */
	public void setMemoryHour(int memoryHour) {
		this.memoryHour = memoryHour;
	}

	/**
	 * @param memoryMinute the memoryMinute to set
	 */
	public void setMemoryMinute(int memoryMinute) {
		this.memoryMinute = memoryMinute;
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
		builder.append(", memoryYear=");
		builder.append(memoryYear);
		builder.append(", memoryMonth=");
		builder.append(memoryMonth);
		builder.append(", memoryDay=");
		builder.append(memoryDay);
		builder.append(", memoryHour=");
		builder.append(memoryHour);
		builder.append(", memoryMinute=");
		builder.append(memoryMinute);
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
}
