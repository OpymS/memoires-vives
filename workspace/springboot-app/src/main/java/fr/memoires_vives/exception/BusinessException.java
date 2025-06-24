package fr.memoires_vives.exception;

import java.util.ArrayList;
import java.util.List;

public class BusinessException extends Exception {
	private static final long serialVersionUID = 1L;
	private List<String> errors;
	
	public BusinessException() {
		this.errors = new ArrayList<String>();
	}
	
	public void add(String message) {
		errors.add(message);
	}

	public boolean hasError() {
		return !errors.isEmpty();
	}

	public List<String> getErrors() {
		return errors;
	}
}
