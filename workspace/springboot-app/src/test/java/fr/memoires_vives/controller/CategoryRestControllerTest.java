package fr.memoires_vives.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.security.CustomUserDetails;
import fr.memoires_vives.security.TestSecurityConfig;

@WebMvcTest(CategoryRestController.class)
@Import(TestSecurityConfig.class)
class CategoryRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MemoryService memoryService;

	private RequestPostProcessor authenticatedUser() {
		User domainUser = new User();
		domainUser.setPseudo("john");
		return user(new CustomUserDetails(domainUser));
	}

	@Test
    void shouldReturnMemoriesWhenAuthenticated() throws Exception {
        when(memoryService.getMemoriesByCategoryForAdmin(1L))
                .thenReturn(List.of(new Memory()));

        mockMvc.perform(get("/api/category/1/associatedMemories")
                        .with(authenticatedUser()))
                .andExpect(status().isOk());

        verify(memoryService).getMemoriesByCategoryForAdmin(1L);
    }
}
