package fr.memoires_vives.exception;

public class DataPersistenceException extends AppException {

	private static final long serialVersionUID = 1L;

	public DataPersistenceException(String message) {
		super(message);
	}
	
	public DataPersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
