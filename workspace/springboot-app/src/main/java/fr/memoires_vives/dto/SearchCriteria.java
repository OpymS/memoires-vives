package fr.memoires_vives.dto;

import java.time.LocalDate;
import java.util.List;

public class SearchCriteria {
	private List<String> words;
	private boolean titleOnly;
	private LocalDate after;
	private LocalDate before;
	private List<Long> categoriesId;
	private boolean onlyMine;
	private int status;
	private double north;
	private double south;
	private double east;
	private double west;
	private String countrySlug;
	private String citySlug;
	private int sortCriteria;
	private int sortDirection;
	
	
	public SearchCriteria() {
	}

	/**
	 * @return the words
	 */
	public List<String> getWords() {
		return words;
	}

	/**
	 * @return the titleOnly
	 */
	public boolean isTitleOnly() {
		return titleOnly;
	}

	/**
	 * @return the after
	 */
	public LocalDate getAfter() {
		return after;
	}

	/**
	 * @return the before
	 */
	public LocalDate getBefore() {
		return before;
	}

	/**
	 * @return the categories
	 */
	public List<Long> getCategoriesId() {
		return categoriesId;
	}

	/**
	 * @return the onlyMine
	 */
	public boolean isOnlyMine() {
		return onlyMine;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return the north
	 */
	public double getNorth() {
		return north;
	}

	/**
	 * @return the south
	 */
	public double getSouth() {
		return south;
	}

	/**
	 * @return the east
	 */
	public double getEast() {
		return east;
	}

	/**
	 * @return the west
	 */
	public double getWest() {
		return west;
	}

	/**
	 * @return the countrySlug
	 */
	public String getCountrySlug() {
		return countrySlug;
	}

	/**
	 * @return the citySlug
	 */
	public String getCitySlug() {
		return citySlug;
	}

	/**
	 * @return the sortCriteria
	 */
	public int getSortCriteria() {
		return sortCriteria;
	}

	/**
	 * @return the sortDirection
	 */
	public int getSortDirection() {
		return sortDirection;
	}

	/**
	 * @param words the words to set
	 */
	public void setWords(List<String> words) {
		this.words = words;
	}

	/**
	 * @param titleOnly the titleOnly to set
	 */
	public void setTitleOnly(boolean titleOnly) {
		this.titleOnly = titleOnly;
	}

	/**
	 * @param after the after to set
	 */
	public void setAfter(LocalDate after) {
		this.after = after;
	}

	/**
	 * @param before the before to set
	 */
	public void setBefore(LocalDate before) {
		this.before = before;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategoriesId(List<Long> categoriesId) {
		this.categoriesId = categoriesId;
	}

	/**
	 * @param onlyMine the onlyMine to set
	 */
	public void setOnlyMine(boolean onlyMine) {
		this.onlyMine = onlyMine;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @param north the north to set
	 */
	public void setNorth(double north) {
		this.north = north;
	}

	/**
	 * @param south the south to set
	 */
	public void setSouth(double south) {
		this.south = south;
	}

	/**
	 * @param east the east to set
	 */
	public void setEast(double east) {
		this.east = east;
	}

	/**
	 * @param west the west to set
	 */
	public void setWest(double west) {
		this.west = west;
	}

	/**
	 * @param countrySlug the countrySlug to set
	 */
	public void setCountrySlug(String countrySlug) {
		this.countrySlug = countrySlug;
	}

	/**
	 * @param citySlug the citySlug to set
	 */
	public void setCitySlug(String citySlug) {
		this.citySlug = citySlug;
	}

	/**
	 * @param sortCriteria the sortCriteria to set
	 */
	public void setSortCriteria(int sortCriteria) {
		this.sortCriteria = sortCriteria;
	}

	/**
	 * @param sortDirection the sortDirection to set
	 */
	public void setSortDirection(int sortDirection) {
		this.sortDirection = sortDirection;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchCriteria [words=");
		builder.append(words);
		builder.append(", titleOnly=");
		builder.append(titleOnly);
		builder.append(", after=");
		builder.append(after);
		builder.append(", before=");
		builder.append(before);
		builder.append(", categoriesId=");
		builder.append(categoriesId);
		builder.append(", onlyMine=");
		builder.append(onlyMine);
		builder.append(", status=");
		builder.append(status);
		builder.append(", north=");
		builder.append(north);
		builder.append(", south=");
		builder.append(south);
		builder.append(", east=");
		builder.append(east);
		builder.append(", west=");
		builder.append(west);
		builder.append("]");
		return builder.toString();
	}
}
