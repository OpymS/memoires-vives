package fr.memoires_vives.bll;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.dto.GeocodingResult;
import fr.memoires_vives.exception.GeocodingException;
import fr.memoires_vives.repositories.LocationRepository;
import fr.memoires_vives.utils.SlugUtil;

@Service
public class LocationServiceImpl implements LocationService {

	private final LocationRepository locationRepository;

	private final ReverseGeocodingService reverseGeocodingService;

	public LocationServiceImpl(LocationRepository locationRepository, ReverseGeocodingService reverseGeocodingService) {
		this.locationRepository = locationRepository;
		this.reverseGeocodingService = reverseGeocodingService;
	}

	@Override
	@Transactional
	public Location saveLocation(Location location) {
		return locationRepository.save(location);
	}

	@Override
	public Location getById(long locationId) {
		return locationRepository.findByLocationId(locationId);
	}

	@Override
	public List<Location> getAllLocations() {
		return locationRepository.findAll();
	}

	@Override
	public List<Location> getLocationsInSquare(double north, double south, double east, double west) {
		if (east - west >= 300) {
			east = 180;
			west = -180;
		}

		while (west < -180) {
			west += 360;
		}
		while (east > 180) {
			east -= 360;
		}
		return locationRepository.findInSquare(BigDecimal.valueOf(north), BigDecimal.valueOf(south),
				BigDecimal.valueOf(east), BigDecimal.valueOf(west));
	}

	@Override
	public Location createFromCoordinates(BigDecimal latitude, BigDecimal longitude) {
		GeocodingResult result;
		try {
			result = reverseGeocodingService.reverseGeocode(latitude, longitude);
		} catch (GeocodingException e) {
			result = new GeocodingResult(null, null, null, null);
		}

		Location location = new Location();
		location.setLatitude(latitude);
		location.setLongitude(normalizeLongitude(longitude));
		location.setName(createLocationName(result));
		location.setCity(result.getCity());
		location.setCitySlug(SlugUtil.toSlug(result.getCity()));
		location.setCountry(result.getCountry());
		location.setCountrySlug(SlugUtil.toSlug(result.getCountry()));
		location.setCountryCode(result.getCountryCode());

		return location;
	}

	private String createLocationName(GeocodingResult geocodingResult) {
		String name;
		if (geocodingResult.getDisplayName() != null && !geocodingResult.getDisplayName().isBlank()) {
			name = geocodingResult.getDisplayName();
		} else if (geocodingResult.getCity() != null) {
			name = geocodingResult.getCity();
		} else if (geocodingResult.getCountry() != null) {
			name = geocodingResult.getCountry();
		} else {
			name = UUID.randomUUID().toString();
		}
		return name.substring(0, Math.min(name.length(), 30));
	}

// Les méthodes privées

	private BigDecimal normalizeLongitude(BigDecimal longitude) {
		BigDecimal result = longitude;
		BigDecimal fullCircle = BigDecimal.valueOf(360);
		BigDecimal max = BigDecimal.valueOf(180);
		BigDecimal min = BigDecimal.valueOf(-180);

		while (result.compareTo(max) > 0) {
			result = result.subtract(fullCircle);
		}
		while (result.compareTo(min) < 0) {
			result = result.add(fullCircle);
		}
		return result;
	}
}
