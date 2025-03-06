package fr.memoires_vives.bll;

import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.repositories.LocationRepository;

@Service
public class LocationServiceImpl implements LocationService {
	
	private final LocationRepository locationRepository;
	
	public LocationServiceImpl(LocationRepository locationRepository) {
		this.locationRepository = locationRepository;
	}

	@Override
	public Location saveLocation(Location location) {
		 return locationRepository.save(location);
	}

}
