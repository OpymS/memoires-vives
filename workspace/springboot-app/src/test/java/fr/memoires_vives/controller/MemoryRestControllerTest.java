package fr.memoires_vives.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.memoires_vives.bll.MemoryService;
import fr.memoires_vives.bll.MemoryUrlService;
import fr.memoires_vives.bo.Memory;
import fr.memoires_vives.dto.SearchCriteria;
import fr.memoires_vives.mapper.MemoryViewMapper;

@WebMvcTest(MemoryRestController.class)
@Import(MemoryViewMapper.class)
public class MemoryRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private MemoryService memoryService;

	@MockitoBean
	private MemoryUrlService memoryUrlService;

	@Test
	@WithMockUser
	void shouldReturnPageOfMemoriesForGridEndpoint() throws Exception {
		SearchCriteria criteria = new SearchCriteria();

		Memory memory = new Memory();
		Page<Memory> page = new PageImpl<>(List.of(memory), PageRequest.of(0, 6), 1);

		when(memoryService.findMemoriesWithCriteria(any(PageRequest.class), any(SearchCriteria.class)))
				.thenReturn(page);
		when(memoryUrlService.buildCanonicalUrl(any(Memory.class))).thenReturn("https://example.com/memory/1");

		mockMvc.perform(post("/api/memory/grid").with(csrf()).param("pageNumber", "1")
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(criteria)))
				.andExpect(status().isOk());

		verify(memoryService).findMemoriesWithCriteria(any(PageRequest.class), any(SearchCriteria.class));
	}

	@Test
	@WithMockUser
	void shouldUseDefaultPageNumberWhenNotProvided() throws Exception {
		SearchCriteria criteria = new SearchCriteria();

		Page<Memory> emptyPage = Page.empty();
		when(memoryService.findMemoriesWithCriteria(any(PageRequest.class), any(SearchCriteria.class)))
				.thenReturn(emptyPage);

		mockMvc.perform(post("/api/memory/grid").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(criteria))).andExpect(status().isOk());

		ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);

		verify(memoryService).findMemoriesWithCriteria(pageCaptor.capture(), any(SearchCriteria.class));

		PageRequest pageRequest = pageCaptor.getValue();
		assertThat(pageRequest.getPageNumber()).isEqualTo(0);
		assertThat(pageRequest.getPageSize()).isEqualTo(12);
	}

	@Test
	@WithMockUser
	void shouldReturnMemoriesForMapEndpoint() throws Exception {
		SearchCriteria criteria = new SearchCriteria();
		List<Memory> memories = List.of(new Memory());

		when(memoryService.findMemoriesOnMapWithCriteria(any(SearchCriteria.class))).thenReturn(memories);

		mockMvc.perform(post("/api/memory/map").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(criteria))).andExpect(status().isOk());

		verify(memoryService).findMemoriesOnMapWithCriteria(any(SearchCriteria.class));
	}

}
