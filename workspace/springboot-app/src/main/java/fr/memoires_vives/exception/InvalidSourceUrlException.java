package fr.memoires_vives.exception;

public class InvalidSourceUrlException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidSourceUrlException(String message) {
		super(message);
	}

	public InvalidSourceUrlException(String message, Throwable cause) {
		super(message, cause);
	}
}
