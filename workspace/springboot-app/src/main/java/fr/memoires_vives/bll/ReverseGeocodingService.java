package fr.memoires_vives.bll;

import java.math.BigDecimal;

import fr.memoires_vives.dto.GeocodingResult;
import fr.memoires_vives.exception.GeocodingException;

public interface ReverseGeocodingService {
	GeocodingResult reverseGeocode(BigDecimal latitude, BigDecimal longitude) throws GeocodingException;
}
