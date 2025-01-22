package fr.memoires_vives.bo;

public class Location {
	private int locationId;
	private String name;
	private float latitude;
	private float longitude;
	
	public Location() {
	}

	public Location(int locationId, String name, float latitude, float longitude) {
		this.locationId = locationId;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @return the locationId
	 */
	public int getLocationId() {
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
	public float getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public float getLongitude() {
		return longitude;
	}

	/**
	 * @param locationId the locationId to set
	 */
	public void setLocationId(int locationId) {
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
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(float longitude) {
		this.longitude = longitude;
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
		builder.append("]");
		return builder.toString();
	}
}
