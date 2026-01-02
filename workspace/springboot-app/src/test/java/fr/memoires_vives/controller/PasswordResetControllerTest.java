package fr.memoires_vives.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.memoires_vives.bll.InvisibleCaptchaService;
import fr.memoires_vives.bll.ResetPasswordService;
import fr.memoires_vives.exception.EmailSendingException;
import fr.memoires_vives.exception.InvalidTokenException;
import fr.memoires_vives.exception.ValidationException;

@WebMvcTest(PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PasswordResetControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ResetPasswordService resetPasswordService;

	@MockitoBean
	private InvisibleCaptchaService captchaService;

//  Tests de  POST /forgot-password 

	@Test
    void shouldReturnFormWhenCaptchaDetectsBot() throws Exception {
        when(captchaService.isBot(anyString(), anyLong())).thenReturn(true);

        mockMvc.perform(post("/forgot-password")
                        .param("email", "test@test.com")
                        .param("website", "spam")
                        .param("formTimestamp", "123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password"))
                .andExpect(model().attributeExists("errorMessage"));

        verifyNoInteractions(resetPasswordService);
    }

	@Test
	void shouldDisplayConfirmationMessageWhenRequestSucceeds() throws Exception {
        when(captchaService.isBot(anyString(), anyLong())).thenReturn(false);
        doNothing().when(resetPasswordService).requestPasswordReset(anyString());

        mockMvc.perform(post("/forgot-password")
                        .param("email", "test@test.com")
                        .param("website", "")
                        .param("formTimestamp", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password"))
                .andExpect(model().attributeExists("message"));
    }

	@Test
	void shouldDisplayErrorMessageWhenEmailSendingFails() throws Exception {
        when(captchaService.isBot(anyString(), anyLong())).thenReturn(false);
        doThrow(new EmailSendingException("Erreur mail", new RuntimeException()))
                .when(resetPasswordService)
                .requestPasswordReset(anyString());

        mockMvc.perform(post("/forgot-password")
                        .param("email", "test@test.com")
                        .param("website", "")
                        .param("formTimestamp", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("message"));
    }

//  Tests de GET /forgot-password/reset-password

	@Test
	void shouldDisplayResetPasswordPageWhenTokenIsValid() throws Exception {
		mockMvc.perform(get("/forgot-password/reset-password").param("token", "valid-token")).andExpect(status().isOk())
				.andExpect(view().name("reset-password")).andExpect(model().attribute("token", "valid-token"));

		verify(resetPasswordService).validatePasswordResetToken("valid-token");
	}

	@Test
	void shouldDisplayErrorPageWhenTokenIsInvalid() throws Exception {
		doThrow(new InvalidTokenException("Token invalide")).when(resetPasswordService)
				.validatePasswordResetToken(anyString());

		mockMvc.perform(get("/forgot-password/reset-password").param("token", "invalid-token"))
				.andExpect(status().isOk()).andExpect(view().name("reset-password-error"))
				.andExpect(model().attributeExists("message"));
	}

//  Tests de POST /forgot-password/reset-password

	@Test
	void shouldDisplaySuccessPageWhenPasswordIsReset() throws Exception {
		mockMvc.perform(post("/forgot-password/reset-password").param("token", "valid-token").param("password",
				"StrongPassword123")).andExpect(status().isOk()).andExpect(view().name("reset-password-success"));

		verify(resetPasswordService).resetPassword("valid-token", "StrongPassword123");
	}

	@Test
	void shouldDisplayErrorPageWhenTokenIsInvalidDuringReset() throws Exception {
		doThrow(new InvalidTokenException("Token invalide")).when(resetPasswordService).resetPassword(anyString(),
				anyString());

		mockMvc.perform(
				post("/forgot-password/reset-password").param("token", "invalid-token").param("password", "pwd"))
				.andExpect(status().isOk()).andExpect(view().name("reset-password-error"))
				.andExpect(model().attributeExists("message"));
	}

	@Test
	void shouldDisplayErrorPageWhenValidationExceptionOccurs() throws Exception {
		ValidationException ve = new ValidationException();
		ve.addGlobalError("Mot de passe invalide");

		doThrow(ve).when(resetPasswordService).resetPassword(anyString(), anyString());

		mockMvc.perform(post("/forgot-password/reset-password").param("token", "valid-token").param("password", "weak"))
				.andExpect(status().isOk()).andExpect(view().name("reset-password-error"))
				.andExpect(model().attributeExists("message"));
	}
}
