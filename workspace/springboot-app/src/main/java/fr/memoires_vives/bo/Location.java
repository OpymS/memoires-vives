package fr.memoires_vives.bo;

import java.math.BigDecimal;
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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "locations")
public class Location {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long locationId;

	@Column(nullable = false)
	@NotBlank
	@Size(max = 30)
	private String name;

	@Column(nullable = false, precision = 9, scale = 6)
	@DecimalMin("-90.0")
	@DecimalMax("90.0")
	private BigDecimal latitude;

	@Column(nullable = false, precision = 9, scale = 6)
	@DecimalMin("-180.0")
	@DecimalMax("180.0")
	private BigDecimal longitude;

	@Column(length = 100)
	private String country;

	@Column(length = 100)
	private String countrySlug;

	@Column(length = 100)
	private String city;

	@Column(length = 100)
	private String citySlug;

	@Column(length = 5)
	private String countryCode;

	@OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Memory> memories = new ArrayList<Memory>();

	public Location() {
	}

	public Location(int locationId, String name, BigDecimal latitude, BigDecimal longitude) {
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
	public BigDecimal getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public BigDecimal getLongitude() {
		return longitude;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return the countrySlug
	 */
	public String getCountrySlug() {
		return countrySlug;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return the citySlug
	 */
	public String getCitySlug() {
		return citySlug;
	}

	/**
	 * @return the countryCode
	 */
	public String getCountryCode() {
		return countryCode;
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
	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @param countrySlug the countrySlug to set
	 */
	public void setCountrySlug(String countrySlug) {
		this.countrySlug = countrySlug;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @param citySlug the citySlug to set
	 */
	public void setCitySlug(String citySlug) {
		this.citySlug = citySlug;
	}

	/**
	 * @param countryCode the countryCode to set
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
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
