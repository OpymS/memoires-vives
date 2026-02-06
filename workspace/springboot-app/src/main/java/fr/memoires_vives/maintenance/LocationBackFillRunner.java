package fr.memoires_vives.maintenance;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bll.LocationService;
import fr.memoires_vives.bo.Location;
import fr.memoires_vives.repositories.LocationRepository;

@Component
@Profile("backfill")
public class LocationBackFillRunner implements CommandLineRunner {

	private final LocationRepository locationRepository;
	private final LocationService locationService;

	public LocationBackFillRunner(LocationService locationService, LocationRepository locationRepository) {
		this.locationRepository = locationRepository;
		this.locationService = locationService;

	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		List<Location> locations = locationRepository.findByCountryIsNull();

		System.out.println("Backfill locations à traiter : " + locations.size());

		int count = 0;

		for (Location location : locations) {
			try {
				Location enriched = locationService.createFromCoordinates(location.getLatitude(), location.getLongitude());

				location.setName(enriched.getName());
				location.setCountry(enriched.getCountry());
				location.setCity(enriched.getCity());
				location.setCountryCode(enriched.getCountryCode());
				location.setCountrySlug(enriched.getCountrySlug());
				location.setCitySlug(enriched.getCitySlug());
				System.out.println("id : "+location.getLocationId()+" - country "+location.getCountry());
				count++;

			} catch (Exception e) {
				System.err.println("Échec géocodage location " + location.getLocationId());
			}
		}

		System.out.println("Backfill terminé : " + count + " locations enrichies");

	}

}
