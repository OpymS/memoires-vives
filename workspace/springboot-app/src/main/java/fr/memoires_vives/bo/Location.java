package fr.memoires_vives.bo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "locations")
public class Location {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long locationId;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private double latitude;
	
	@Column(nullable = false)
	private double longitude;
	
	@OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Memory> memories = new ArrayList<Memory>();
	
	public Location() {
	}

	public Location(int locationId, String name, double latitude, double longitude) {
		this.locationId = locationId;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @return the locationId
	 */
	public long getLocationId() {
		return locationId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * @return the memories
	 */
	public List<Memory> getMemories() {
		return memories;
	}

	/**
	 * @param locationId the locationId to set
	 */
	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @param memories the memories to set
	 */
	public void setMemories(List<Memory> memories) {
		this.memories = memories;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Location [locationId=");
		builder.append(locationId);
		builder.append(", name=");
		builder.append(name);
		builder.append(", latitude=");
		builder.append(latitude);
		builder.append(", longitude=");
		builder.append(longitude);
//		builder.append(", memories=");
//		builder.append(memories);
		builder.append("]");
		return builder.toString();
	}
}
