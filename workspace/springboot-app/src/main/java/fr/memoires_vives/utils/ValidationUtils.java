package fr.memoires_vives.utils;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import fr.memoires_vives.exception.ValidationException;

public final class ValidationUtils {
	
	private ValidationUtils() {}
	
	public static void addValidationErrors(ValidationException ve, BindingResult bindingResult) {
		ve.getGlobalErrors().forEach(err -> {
			ObjectError error = new ObjectError("globalError", err);
			bindingResult.addError(error);
		});
		ve.getFieldErrors().forEach(err -> {
			FieldError error = new FieldError("user", err.getField(), null, false, null, null, err.getMessage());
			bindingResult.addError(error);
		});
	}

}
