package fr.memoires_vives.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.memoires_vives.bll.LocationService;
import fr.memoires_vives.bo.Location;

@RestController
@RequestMapping("/api/location")
public class LocationRestController {
	private final LocationService locationService;

	public LocationRestController(LocationService locationService) {
		this.locationService = locationService;
	}

	@GetMapping("/visible-points")
	public ResponseEntity<?> getVisiblePoints(@RequestParam("north") double northLatitude,
			@RequestParam("south") double southLatitude, @RequestParam("east") double eastLongitude,
			@RequestParam("west") double westLongitude) {

		
		List<Location> locations = locationService.getLocationsInSquare(northLatitude, southLatitude, eastLongitude, westLongitude);
		return ResponseEntity.ok(locations);
	}

}
