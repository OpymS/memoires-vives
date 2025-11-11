package fr.memoires_vives.exception;

public class UnauthorizedActionException extends AppException {

	private static final long serialVersionUID = 1L;

	public UnauthorizedActionException(String message) {
		super(message);
	}

}
