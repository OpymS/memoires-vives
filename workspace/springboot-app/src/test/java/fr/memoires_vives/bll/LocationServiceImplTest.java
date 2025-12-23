package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.repositories.LocationRepository;

@ExtendWith(MockitoExtension.class)
public class LocationServiceImplTest {
	@Mock
	private LocationRepository locationRepository;

	@InjectMocks
	private LocationServiceImpl locationService;

	private Location location;

	@BeforeEach
	void setUp() {
		location = new Location();
		location.setLocationId(1L);
		location.setName("Paris");
	}

//  Tests de saveLocation

	@Test
	void saveLocation_shouldReturnSavedLocation() {
		Location savedLocation = new Location();
		savedLocation.setLocationId(1L);

		when(locationRepository.save(location)).thenReturn(savedLocation);

		Location result = locationService.saveLocation(location);

		assertNotNull(result);
		assertEquals(1L, result.getLocationId());
		verify(locationRepository, times(1)).save(location);
	}

//  Tests de getById

	@Test
	void getById_shouldReturnLocation_whenFound() {
		long locationId = 1L;
		Location location = new Location();
		location.setLocationId(locationId);

		when(locationRepository.findByLocationId(locationId)).thenReturn(location);

		Location result = locationService.getById(locationId);

		assertNotNull(result);
		assertEquals(locationId, result.getLocationId());
		verify(locationRepository).findByLocationId(locationId);
	}

	@Test
	void getById_shouldReturnNull_whenNotFound() {
		long locationId = 2L;

		when(locationRepository.findByLocationId(locationId)).thenReturn(null);

		Location result = locationService.getById(locationId);

		assertNull(result);
		verify(locationRepository).findByLocationId(locationId);
	}

	@Test
	void getById_shouldPropagateException_fromRepository() {
		long locationId = 3L;

		when(locationRepository.findByLocationId(locationId)).thenThrow(new RuntimeException("DB error"));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> locationService.getById(locationId));
		assertEquals("DB error", ex.getMessage());

		verify(locationRepository).findByLocationId(locationId);
	}

//  Tests de getAllLocations

	@Test
	void getAllLocations_shouldReturnList_whenLocationsExist() {
		List<Location> locations = Arrays.asList(new Location(), new Location());
		when(locationRepository.findAll()).thenReturn(locations);

		List<Location> result = locationService.getAllLocations();

		assertNotNull(result);
		assertEquals(2, result.size());
		assertSame(locations, result);
		verify(locationRepository).findAll();
	}

	@Test
	void getAllLocations_shouldReturnEmptyList_whenNoLocations() {
	    when(locationRepository.findAll()).thenReturn(Collections.emptyList());

	    List<Location> result = locationService.getAllLocations();

	    assertNotNull(result);
	    assertTrue(result.isEmpty());
	    verify(locationRepository).findAll();
	}

	@Test
	void getAllLocations_shouldPropagateException_fromRepository() {
	    when(locationRepository.findAll()).thenThrow(new RuntimeException("DB error"));

	    RuntimeException ex = assertThrows(RuntimeException.class, () ->
	        locationService.getAllLocations()
	    );
	    assertEquals("DB error", ex.getMessage());
	    verify(locationRepository).findAll();
	}

//  Tests de getLocationsInSquare

	@Test
	void getLocationsInSquare_shouldCallRepositoryWithSameValues_whenNormal() {
		double north = 50, south = 40, east = 10, west = 0;
		List<Location> expected = Arrays.asList(new Location());

		when(locationRepository.findInSquare(north, south, east, west)).thenReturn(expected);

		List<Location> result = locationService.getLocationsInSquare(north, south, east, west);

		assertSame(expected, result);
		verify(locationRepository).findInSquare(north, south, east, west);
	}

	@Test
	void getLocationsInSquare_shouldNormalize_whenWidthExceeds300() {
		double north = 50, south = 40, east = 200, west = -150;
		List<Location> expected = Arrays.asList(new Location());

		when(locationRepository.findInSquare(50, 40, 180, -180)).thenReturn(expected);

		List<Location> result = locationService.getLocationsInSquare(north, south, east, west);

		assertSame(expected, result);
		verify(locationRepository).findInSquare(50, 40, 180, -180);
	}

	@Test
	void getLocationsInSquare_shouldNormalizeWest_whenLessThanMinus180() {
		double north = 50, south = 40, west = -400, east = -150;

		List<Location> expected = Arrays.asList(new Location());

		double normalizedWest = -40;

		when(locationRepository.findInSquare(north, south, east, normalizedWest)).thenReturn(expected);

		List<Location> result = locationService.getLocationsInSquare(north, south, east, west);

		assertSame(expected, result);
		verify(locationRepository).findInSquare(north, south, east, normalizedWest);
	}

	@Test
	void getLocationsInSquare_shouldNormalizeEast_whenGreaterThan180() {
		double north = 50, south = 40, east = 200, west = 10;

		List<Location> expected = Arrays.asList(new Location());

		double normalizedEast = -160;

		when(locationRepository.findInSquare(north, south, normalizedEast, west)).thenReturn(expected);

		List<Location> result = locationService.getLocationsInSquare(north, south, east, west);

		assertSame(expected, result);
		verify(locationRepository).findInSquare(north, south, normalizedEast, west);
	}

	@Test
	void getLocationsInSquare_shouldPropagateException_fromRepository() {
		double north = 50, south = 40, east = 10, west = 0;

		when(locationRepository.findInSquare(north, south, east, west)).thenThrow(new RuntimeException("DB error"));

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> locationService.getLocationsInSquare(north, south, east, west));

		assertEquals("DB error", ex.getMessage());
		verify(locationRepository).findInSquare(north, south, east, west);
	}
}
