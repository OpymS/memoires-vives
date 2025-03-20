package fr.memoires_vives.bll;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.repositories.LocationRepository;

@Service
public class LocationServiceImpl implements LocationService {

	private final LocationRepository locationRepository;

	public LocationServiceImpl(LocationRepository locationRepository) {
		this.locationRepository = locationRepository;
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

}
