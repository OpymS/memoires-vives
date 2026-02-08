package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.Memory;

@ExtendWith(MockitoExtension.class)
class MemoryUrlServiceImplTest {

	private MemoryUrlServiceImpl service;

	@Mock
	private Memory memory;

	@Mock
	private Location location;

	@BeforeEach
	void setUp() {
		service = new MemoryUrlServiceImpl();
	}

	@Test
	void buildCanonicalUrl_shouldBuildUrlWithoutLocation() {
		when(memory.getLocation()).thenReturn(null);
		when(memory.getMemoryId()).thenReturn(42L);
		when(memory.getSlug()).thenReturn("my-memory");

		String result = service.buildCanonicalUrl(memory);

		assertEquals("/memory/42-my-memory", result);
	}

	@Test
	void buildCanonicalUrl_shouldBuildUrlWithCountryOnly() {
		when(memory.getLocation()).thenReturn(location);
		when(location.getCountrySlug()).thenReturn("france");
		when(location.getCitySlug()).thenReturn(null);
		when(memory.getMemoryId()).thenReturn(42L);
		when(memory.getSlug()).thenReturn("my-memory");
		
		String result = service.buildCanonicalUrl(memory);
		
		assertEquals("/memory/france/42-my-memory", result);
	}

	@Test
	void buildCanonicalUrl_shouldBuildUrlWithCountryAndCity() {
		when(memory.getLocation()).thenReturn(location);
		when(location.getCountrySlug()).thenReturn("france");
		when(location.getCitySlug()).thenReturn("paris");
		when(memory.getMemoryId()).thenReturn(42L);
		when(memory.getSlug()).thenReturn("my-memory");
		
		String result = service.buildCanonicalUrl(memory);
		
		assertEquals("/memory/france/paris/42-my-memory", result);
	}

	@Test
	void buildCanonicalUrl_shouldIgnoreCityWhenCountryIsNull() {
		when(memory.getLocation()).thenReturn(location);
		when(location.getCountrySlug()).thenReturn(null);
		when(memory.getMemoryId()).thenReturn(42L);
		when(memory.getSlug()).thenReturn("my-memory");
		
		String result = service.buildCanonicalUrl(memory);
		
		assertEquals("/memory/42-my-memory", result);
	}
}
