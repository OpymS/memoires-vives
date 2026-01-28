package fr.memoires_vives.dto;

public class GeocodingResult {
	private String displayName;
	private String city;
	private String country;
	private String countryCode;

	public GeocodingResult() {
	}

	public GeocodingResult(String city, String country, String countryCode) {
		this.city = city;
		this.country = country;
		this.countryCode = countryCode;
	}

	public GeocodingResult(String displayName, String city, String country, String countryCode) {
		this.displayName = displayName;
		this.city = city;
		this.country = country;
		this.countryCode = countryCode;
	}

	/**
	 * @return the name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return the countryCode
	 */
	public String getCountryCode() {
		return countryCode;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @param countryCode the countryCode to set
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	};
}
