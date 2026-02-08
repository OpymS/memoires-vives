package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import fr.memoires_vives.dto.GeocodingResult;
import fr.memoires_vives.exception.GeocodingException;
import fr.memoires_vives.utils.TestUtils;

@ExtendWith(MockitoExtension.class)
class ReverseGeocodingServiceImplTest {

	private ReverseGeocodingServiceImpl reverseGeocodingService;

	@Mock
	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
		when(builder.defaultHeader(anyString(), any(String[].class))).thenReturn(builder);
		when(builder.build()).thenReturn(restTemplate);

		reverseGeocodingService = new ReverseGeocodingServiceImpl(builder);

		TestUtils.setField(reverseGeocodingService, "userAgent", "test-agent");
	}

	@Test
	void reverseGeocode_shouldThrowException_whenLatitudeIsNull() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> reverseGeocodingService.reverseGeocode(null, BigDecimal.ONE));

		assertEquals("Latitude et longitude ne peuvent pas être nulles", exception.getMessage());
	}

	@SuppressWarnings("unchecked")
	@Test
    void reverseGeocode_shouldWrapException_whenRestTemplateFails() {
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("HTTP error"));

        GeocodingException exception = assertThrows(
                GeocodingException.class,
                () -> reverseGeocodingService.reverseGeocode(BigDecimal.ONE, BigDecimal.TEN)
        );

        assertEquals("Erreur lors de l’appel au service de reverse geocoding", exception.getMessage());
    }

	@SuppressWarnings("unchecked")
	@Test
	void reverseGeocode_shouldThrowException_whenBodyIsNull() {
		ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(null, HttpStatus.OK);

		when(restTemplate.exchange(any(), any(), any(), any(ParameterizedTypeReference.class))).thenReturn(response);

		GeocodingException exception = assertThrows(GeocodingException.class,
				() -> reverseGeocodingService.reverseGeocode(BigDecimal.ONE, BigDecimal.TEN));

		assertEquals("Réponse vide du service de reverse geocoding", exception.getMessage());
	}

	@SuppressWarnings("unchecked")
	@Test
	void reverseGeocode_shouldThrowException_whenAddressIsMissing() {
		Map<String, Object> body = new HashMap<>();
		body.put("display_name", "Paris, France");

		when(restTemplate.exchange(any(), any(), any(), any(ParameterizedTypeReference.class)))
				.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

		GeocodingException exception = assertThrows(GeocodingException.class,
				() -> reverseGeocodingService.reverseGeocode(BigDecimal.ONE, BigDecimal.TEN));

		assertEquals("Aucune information d’adresse trouvée", exception.getMessage());
	}

	@SuppressWarnings("unchecked")
	@Test
	void reverseGeocode_shouldReturnResult_whenCityIsPresent() {
		Map<String, Object> address = Map.of("city", "Paris", "country", "France", "country_code", "fr");

		Map<String, Object> body = new HashMap<>();
		body.put("display_name", "Paris, France");
		body.put("address", address);

		when(restTemplate.exchange(any(), any(), any(), any(ParameterizedTypeReference.class)))
				.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

		GeocodingResult result = reverseGeocodingService.reverseGeocode(new BigDecimal("48.8566"),
				new BigDecimal("2.3522"));

		assertEquals("Paris, France", result.getDisplayName());
		assertEquals("Paris", result.getCity());
		assertEquals("France", result.getCountry());
		assertEquals("FR", result.getCountryCode());
	}

	@SuppressWarnings("unchecked")
	@Test
	void reverseGeocode_shouldFallbackToTown_whenCityIsNull() {
		Map<String, Object> address = Map.of("town", "Versailles", "country", "France");

		Map<String, Object> body = Map.of("display_name", "Versailles, France", "address", address);

		when(restTemplate.exchange(any(), any(), any(), any(ParameterizedTypeReference.class)))
				.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

		GeocodingResult result = reverseGeocodingService.reverseGeocode(BigDecimal.ONE, BigDecimal.TEN);

		assertEquals("Versailles", result.getCity());
		assertNull(result.getCountryCode());
	}
}
