package fr.memoires_vives.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends AppException {
	private static final long serialVersionUID = 1L;
	private List<String> errors = new ArrayList<String>();

	public ValidationException() {
		super("Erreur(s) de validation");
	}

	public void add(String message) {
		errors.add(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public boolean hasError() {
		return !errors.isEmpty();
	}

	public List<String> getErrors() {
		return errors;
	}

}
