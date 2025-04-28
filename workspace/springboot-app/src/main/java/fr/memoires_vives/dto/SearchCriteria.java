package fr.memoires_vives.dto;

import java.time.LocalDate;
import java.util.List;

import fr.memoires_vives.bo.Category;

public class SearchCriteria {
	private List<String> words;
	private boolean titleOnly;
	private LocalDate after;
	private LocalDate before;
	private List<Category> categories;
	private boolean onlyMine;
	private int status;
	
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
	public List<Category> getCategories() {
		return categories;
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
	public void setCategories(List<Category> categories) {
		this.categories = categories;
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
		builder.append(", categories=");
		builder.append(categories);
		builder.append(", onlyMine=");
		builder.append(onlyMine);
		builder.append(", status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
	
	
	
}
