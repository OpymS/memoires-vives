package fr.memoires_vives.bll;

import java.util.List;

import fr.memoires_vives.bo.Location;

public interface LocationService {
	Location saveLocation(Location location);
	Location getById(long locationId);
	List<Location> getAllLocations();
}
