package fr.memoires_vives.bll;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fr.memoires_vives.dto.GeocodingResult;
import fr.memoires_vives.exception.GeocodingException;

@Service
public class ReverseGeocodingServiceImpl implements ReverseGeocodingService {

	private final RestTemplate restTemplate;

	@Value("${reverse.geocoding.url:https://nominatim.openstreetmap.org/reverse}")
	private String baseUrl;

	@Value("${reverse.geocoding.user-agent}")
	private String userAgent;

	public ReverseGeocodingServiceImpl(RestTemplateBuilder builder) {
		this.restTemplate = builder.defaultHeader(HttpHeaders.USER_AGENT, userAgent).build();
	}

	@Override
	@Cacheable(
			value = "reverseGeocoding",
			key = "#p0.setScale(4, T(java.math.RoundingMode).HALF_UP) + ',' + #p1.setScale(4, T(java.math.RoundingMode).HALF_UP)"
	)
	public GeocodingResult reverseGeocode(BigDecimal latitude, BigDecimal longitude) {

//		System.out.println("APPEL NOMINATIM pour " + latitude + "," + longitude);
		if (latitude == null || longitude == null) {
			throw new IllegalArgumentException("Latitude et longitude ne peuvent pas être nulles");
		}

		URI uri = UriComponentsBuilder
				.newInstance()
				.scheme("https")
				.host("nominatim.openstreetmap.org")
				.path("/reverse")
				.queryParam("format", "json")
				.queryParam("lat", latitude.toPlainString())
				.queryParam("lon", longitude.toPlainString())
				.queryParam("addressdetails", 1)
				.queryParam("accept-language", "fr")
				.build()
				.toUri();

//		System.out.println(uri);
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.USER_AGENT, userAgent);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		HttpEntity<Void> entity = new HttpEntity<>(headers);

		ResponseEntity<Map<String, Object>> response;
	    try {
	        response = restTemplate.exchange(
	                uri,
	                HttpMethod.GET,
	                entity,
	                new ParameterizedTypeReference<>() {}
	        );
	    } catch (Exception e) {
	        throw new GeocodingException("Erreur lors de l’appel au service de reverse geocoding", e);
	    }

	    Map<String, Object> body = response.getBody();
	    if (body == null) {
	        throw new GeocodingException("Réponse vide du service de reverse geocoding");
	    }

	    @SuppressWarnings("unchecked")
	    Map<String, Object> address = (Map<String, Object>) body.get("address");
	    if (address == null) {
	        throw new GeocodingException("Aucune information d’adresse trouvée");
	    }
	    String displayName = (String) body.get("display_name");

	    String country = (String) address.get("country");
	    String countryCode = (String) address.get("country_code");

		String city = firstNotNull(
				(String) address.get("city"),
				(String) address.get("town"),
				(String) address.get("village"),
				(String) address.get("municipality"),
				(String) address.get("hamlet"),
				(String) address.get("county")
				);

		return new GeocodingResult(displayName, city, country, countryCode != null ? countryCode.toUpperCase() : null);
	}
	
	@SafeVarargs
	private final <T> T firstNotNull(T... values) {
		for (T value : values) {
			if (value != null) {
				return value;
			}
		}
		return null;
	}
}
