package fr.memoires_vives.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends AppException {
	private static final long serialVersionUID = 1L;

	public static class FieldError {
		private final String field;
		private final String message;

		public FieldError(String field, String message) {
			this.field = field;
			this.message = message;
		}

		public String getField() {
			return field;
		}
		public String getMessage() {
			return message;
		}
	}

	private final List<FieldError> fieldErrors = new ArrayList<>();
	private final List<String> globalErrors = new ArrayList<>();

	public ValidationException() {
		super("Erreur(s) de validation");
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public void addFieldError(String field, String message) {
		fieldErrors.add(new FieldError(field, message));
	}
	
	public void addGlobalError(String message) {
		globalErrors.add(message);
	}

	public boolean hasError() {
		return !globalErrors.isEmpty() || !fieldErrors.isEmpty();
	}

	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}
	
	public List<String> getGlobalErrors() {
		return globalErrors;
	}

}
