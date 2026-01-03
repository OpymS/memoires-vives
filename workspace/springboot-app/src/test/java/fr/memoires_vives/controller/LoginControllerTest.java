package fr.memoires_vives.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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

import fr.memoires_vives.bll.ActivationService;
import fr.memoires_vives.bll.InvisibleCaptchaService;
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.exception.ValidationException;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LoginControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private InvisibleCaptchaService captchaService;

	@MockitoBean
	private ActivationService activationService;

//  Tests de POST /signup

	@Test
    void shouldReturnSignupWhenCaptchaDetectsBot() throws Exception {
        when(captchaService.isBot(anyString(), anyLong())).thenReturn(true);

        mockMvc.perform(post("/signup")
                .param("pseudo", "john")
                .param("email", "john@test.com")
                .param("password", "secret")
                .param("passwordConfirm", "secret")
                .param("formTimestamp", "123456789")
                .param("website", "un truc"))
            .andExpect(status().isOk())
            .andExpect(view().name("signup"));

        verifyNoInteractions(userService);
    }

	@Test
    void shouldReturnSignupWhenBindingErrorsOccur() throws Exception {
		when(captchaService.isBot(anyString(), anyLong())).thenReturn(false);

        mockMvc.perform(post("/signup")
                .param("email", "john@test.com")
                .param("password", "secret")
                .param("passwordConfirm", "secret")
                .param("formTimestamp", "123456789"))
            .andExpect(status().isOk())
            .andExpect(view().name("signup"))
            .andExpect(model().hasErrors());

        verifyNoInteractions(userService);
    }

	@Test
    void shouldReturnSignupWhenValidationExceptionOccurs() throws Exception {
		when(captchaService.isBot(anyString(), anyLong())).thenReturn(false);

        ValidationException ve = new ValidationException();
        ve.addFieldError("email", "Email déjà utilisé");

        when(userService.createAccount(
                any(), any(), any(), any(), any()))
            .thenThrow(ve);

        mockMvc.perform(post("/signup")
                .param("pseudo", "john")
                .param("email", "john@test.com")
                .param("password", "secret")
                .param("passwordConfirm", "secret")
                .param("formTimestamp", "123456789"))
            .andExpect(status().isOk())
            .andExpect(view().name("signup"))
            .andExpect(model().hasErrors());

        verify(activationService, never()).requestActivation(any());
    }
}
