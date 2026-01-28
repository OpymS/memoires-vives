package fr.memoires_vives.exception;

public class GeocodingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public GeocodingException(String message) {
		super(message);
	}

	public GeocodingException(String message, Throwable cause) {
		super(message, cause);
	}
}
