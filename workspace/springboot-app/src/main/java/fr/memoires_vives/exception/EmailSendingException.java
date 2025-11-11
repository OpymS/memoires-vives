package fr.memoires_vives.exception;

public class EmailSendingException extends AppException {

	private static final long serialVersionUID = 1L;

	public EmailSendingException(String message, Throwable cause) {
		super(message, cause);
	}

}
