package fr.memoires_vives.bll;

import java.math.BigDecimal;
import java.util.List;

import fr.memoires_vives.bo.Location;

public interface LocationService {
	Location saveLocation(Location location);

	Location getById(long locationId);

	List<Location> getAllLocations();

	List<Location> getLocationsInSquare(double north, double south, double east, double west);

	Location createFromCoordinates(BigDecimal latitude, BigDecimal longitude);
}
