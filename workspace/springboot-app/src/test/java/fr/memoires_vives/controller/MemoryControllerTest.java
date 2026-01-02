package fr.memoires_vives.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import fr.memoires_vives.bll.CategoryService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.exception.ValidationException;
import fr.memoires_vives.security.CustomUserDetails;

@WebMvcTest(MemoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MemoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MemoryService memoryService;

	@MockitoBean
	private CategoryService categoryService;

	private RequestPostProcessor authenticatedUser() {
		User domainUser = new User();
		domainUser.setPseudo("john");
		return user(new CustomUserDetails(domainUser));
	}

//  Tests de POST /memory/new

	@Test
	void shouldReturnFormWhenValidationErrorsOnCreate() throws Exception {
		mockMvc.perform(multipart("/memory/new").param("title", "").param("description", "desc"))
				.andExpect(status().isOk()).andExpect(view().name("memory-form"));
	}

	@Test
	void shouldReturnFormWhenValidationExceptionOnCreate() throws Exception {
		ValidationException ve = new ValidationException();
		ve.addGlobalError("Erreur globale");
		ve.addFieldError("title", "Titre invalide");

		doThrow(ve).when(memoryService).createMemory(any(), any(), any(), any());

		mockMvc.perform(multipart("/memory/new").param("title", "Titre").param("description", "Description"))
				.andExpect(status().isOk()).andExpect(view().name("memory-form"));
	}

//  Tests de POST /memory/modify

	@Test
	void shouldReturnFormWhenValidationErrorsOnModify() throws Exception {
		mockMvc.perform(post("/memory/modify").with(authenticatedUser()).param("memoryId", "1").param("title", "") // @NotBlank
				.param("description", "desc").param("memoryDate", "2023-01-01").param("visibility", "PUBLIC")
				.param("state", "PUBLISHED").param("location.name", "").param("location.latitude", "0")
				.param("location.longitude", "0")).andExpect(status().isOk()).andExpect(view().name("memory-form"));

		verifyNoInteractions(memoryService);
	}

	@Test
	void shouldReturnFormWhenValidationExceptionOnModify() throws Exception {
		ValidationException ve = new ValidationException();
		ve.addFieldError("title", "Titre invalide");

		doThrow(ve).when(memoryService).updateMemory(any(), any(), any(), any());

		mockMvc.perform(multipart("/memory/modify").param("memoryId", "42").param("title", "Titre").param("description",
				"Description")).andExpect(status().isOk()).andExpect(view().name("memory-form"));
	}
}
