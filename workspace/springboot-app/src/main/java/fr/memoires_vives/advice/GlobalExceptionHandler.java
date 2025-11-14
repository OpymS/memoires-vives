package fr.memoires_vives.advice;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.UnauthorizedActionException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EntityNotFoundException.class)
	public String handleEntityNotFound(EntityNotFoundException ex, Model model) {
		model.addAttribute("errorMessage", ex.getMessage());
		return "error/404";
	}
	
	@ExceptionHandler(UnauthorizedActionException.class)
	public String handleUnauthorizedAction(UnauthorizedActionException ex, Model model) {
		model.addAttribute("errorMessage", ex.getMessage());
		return "error/403";
	}
}
