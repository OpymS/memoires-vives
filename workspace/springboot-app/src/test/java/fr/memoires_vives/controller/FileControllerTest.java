package fr.memoires_vives.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import fr.memoires_vives.bll.FileService;
import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.security.CustomUserDetails;

@WebMvcTest(FileController.class)
public class FileControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private MemoryService memoryService;

	@MockitoBean
	private FileService fileService;

	private RequestPostProcessor authenticatedUser() {
		User domainUser = new User();
		domainUser.setPseudo("john");
		return user(new CustomUserDetails(domainUser));
	}

	@Test
	void shouldDeleteImageWhenUserIsAuthenticated() throws Exception {
		User user = new User();
		user.setUserId(1L);
		Memory memory = new Memory();
		memory.setRememberer(user);

		when(userService.getCurrentUser()).thenReturn(user);
		when(memoryService.getMemoryByImage("uuid123")).thenReturn(memory);

		mockMvc.perform(delete("/uploads/images/uploadedImages/uuid123").with(authenticatedUser()).with(csrf()))
				.andExpect(status().isOk()).andExpect(content().string("Image supprimée avec succès."));

		verify(fileService).deleteUserFile(user, "uuid123", memory);
	}

	@Test
	void shouldRejectDeleteWhenUserIsNotAuthenticated() throws Exception {
		mockMvc.perform(delete("/uploads/images/uploadedImages/uuid123")).andExpect(status().isForbidden());

		verifyNoInteractions(fileService);
	}

}
