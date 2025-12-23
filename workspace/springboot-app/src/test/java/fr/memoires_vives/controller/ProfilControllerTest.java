package fr.memoires_vives.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.multipart.MultipartFile;

import fr.memoires_vives.bll.ActivationService;
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.InvalidTokenException;
import fr.memoires_vives.exception.ValidationException;
import fr.memoires_vives.security.CustomUserDetails;

@WebMvcTest(ProfilController.class)
@WithMockUser
class ProfilControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private ActivationService activationService;

	private UserDetails userDetailsWithDomainUser() {
		User domainUser = new User();
		domainUser.setPseudo("john");

		return new CustomUserDetails(domainUser);
	}

//  Tests de GET/profil

	@Test
	void shouldDisplayProfilPageWithUserAndNullPassword() throws Exception {
		User user = new User();
		user.setUserId(1L);
		user.setPassword("secret");

		when(userService.getUserOrCurrent(1L)).thenReturn(user);

		mockMvc.perform(get("/profil").with(user(userDetailsWithDomainUser())).param("userId", "1"))
				.andExpect(status().isOk()).andExpect(view().name("profil"))
				.andExpect(model().attributeExists("userToDisplay"))
				.andExpect(model().attribute("userToDisplay", hasProperty("password", nullValue())));

		verify(userService).getUserOrCurrent(1L);
	}

//  Tests de GET /profil/modify

	@WithMockUser(roles = "ADMIN")
	@Test
	void shouldDisplayModifyFormForAdminWithUserId() throws Exception {
		User user = new User();

		when(userService.isAdmin()).thenReturn(true);
		when(userService.getUserById(2L)).thenReturn(user);

		mockMvc.perform(get("/profil/modify")
				.with(SecurityMockMvcRequestPostProcessors.user(userDetailsWithDomainUser())).param("userId", "2"))
				.andExpect(status().isOk()).andExpect(view().name("profil-modify"))
				.andExpect(model().attributeExists("user"));

		verify(userService).getUserById(2L);
	}

	@Test
	void shouldDisplayModifyFormForCurrentUserWhenNotAdmin() throws Exception {
		User user = new User();

		when(userService.isAdmin()).thenReturn(false);
		when(userService.getCurrentUser()).thenReturn(user);

		mockMvc.perform(get("/profil/modify")).andExpect(status().isOk()).andExpect(view().name("profil-modify"))
				.andExpect(model().attributeExists("user"));

		verify(userService).getCurrentUser();
	}

//  Tests de POST /profil/modify

	@Test
	void shouldReturnFormWhenBindingErrors() throws Exception {
		User user = new User();

		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "user");
		bindingResult.addError(new ObjectError("user", "error"));

		mockMvc.perform(post("/profil/modify").flashAttr("user", user)
				.requestAttr(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult)).andExpect(status().isOk())
				.andExpect(view().name("profil-modify"));

		verify(userService, never()).updateProfile(any(), any(), any());
	}

	@Test
	void shouldUpdateProfileAndRedirectOnSuccess() throws Exception {
		User user = new User();
		user.setUserId(3L);

		mockMvc.perform(multipart("/profil/modify").file("image", new byte[0]).with(user(userDetailsWithDomainUser())).param("currentPassword", "pwd")
				.flashAttr("user", user)).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/profil?userId=3"));

		verify(userService).updateProfile(eq(user), eq("pwd"), any(MultipartFile.class));
	}

	@Test
	void shouldReturnFormWhenValidationExceptionOccurs() throws Exception {
		ValidationException ve = mock(ValidationException.class);

		when(ve.getGlobalErrors()).thenReturn(List.of("Global error"));
		when(ve.getFieldErrors()).thenReturn(List.of(new ValidationException.FieldError("email", "Invalid email")));

		doThrow(ve).when(userService).updateProfile(any(), any(), any());

		mockMvc.perform(post("/profil/modify").flashAttr("user", new User())).andExpect(status().isOk())
				.andExpect(view().name("profil-modify")).andExpect(model().hasErrors());
	}

//  Tests de GET /profil/activation

	@Test
	void shouldActivateUserAndRedirectToLogin() throws Exception {
		User user = new User();
		user.setPseudo("john");

		when(activationService.activateUser("token123")).thenReturn(user);

		mockMvc.perform(get("/profil/activation").param("token", "token123")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login")).andExpect(flash().attributeExists("message"))
				.andExpect(flash().attribute("loginUser", "john"));
	}

	@Test
    void shouldDisplayActivationErrorWhenTokenInvalid() throws Exception {
        when(activationService.activateUser("bad"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(get("/profil/activation").with(user(userDetailsWithDomainUser()))
                        .param("token", "bad"))
                .andExpect(status().isOk())
                .andExpect(view().name("activation-error"))
                .andExpect(model().attributeExists("message"));
    }

//  Tests de GET /profil/activation/resend

	@Test
    void shouldRedirectWithErrorWhenUserNotFound() throws Exception {
        when(userService.getUserByPseudo("unknown"))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/profil/activation/resend")
                        .param("username", "unknown"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("error"));
    }

	@Test
	void shouldRedirectWithErrorWhenUserAlreadyActivated() throws Exception {
		User user = new User();
		user.setActivated(true);

		when(userService.getUserByPseudo("john")).thenReturn(user);

		mockMvc.perform(get("/profil/activation/resend").param("username", "john"))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"))
				.andExpect(flash().attributeExists("error"));

		verify(activationService, never()).regenerateTokenAndSendMail(any());
	}

	@Test
	void shouldResendActivationMailWhenUserNotActivated() throws Exception {
		User user = new User();
		user.setActivated(false);

		when(userService.getUserByPseudo("john")).thenReturn(user);

		mockMvc.perform(get("/profil/activation/resend").param("username", "john"))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"))
				.andExpect(flash().attributeExists("message"));

		verify(activationService).regenerateTokenAndSendMail(user);
	}
}
