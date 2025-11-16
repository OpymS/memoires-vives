package fr.memoires_vives.security;

import java.io.IOException;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String errorMessage = "Pseudo ou mot de passe incorrect";
		
		if (exception instanceof DisabledException) {
			errorMessage = "Votre compte n'est pas activé. Vérifiez votre boîte mail pour activer votre compte.";
            request.getSession().setAttribute("resendActivation", true);
		}

		request.getSession().setAttribute("error", errorMessage);
		response.sendRedirect("/login");
	}

}
