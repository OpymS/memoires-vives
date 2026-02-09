package fr.memoires_vives.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.memoires_vives.bo.Location;
import fr.memoires_vives.bo.MemoryState;

public interface LocationRepository extends JpaRepository<Location, Long> {
	Location findByLocationId(long locationId);

	@Query("SELECT l FROM Location l WHERE (l.latitude <= :north AND l.latitude >= :south) "
			+ "AND ((:west < :east AND l.longitude <= :east AND l.longitude >= :west) "
			+ "OR (:west > :east AND (l.longitude <= :east OR l.longitude >= :west)))")
	List<Location> findInSquare(@Param("north") BigDecimal north, @Param("south") BigDecimal south,
			@Param("east") BigDecimal east, @Param("west") BigDecimal west);

	List<Location> findByCountryIsNull();

	@Query("SELECT l.country FROM Location l WHERE l.countrySlug = :countrySlug AND l.country IS NOT NULL GROUP BY l.country ORDER BY COUNT(l) DESC")
	List<String> findCountryByFrequency(@Param("countrySlug") String countrySlug);

	@Query("SELECT l.city FROM Location l WHERE l.countrySlug = :countrySlug AND l.country IS NOT NULL AND l.citySlug = :citySlug AND l.city IS NOT NULL GROUP BY l.city ORDER BY COUNT(l) DESC")
	List<String> findCityByFrequency(@Param("citySlug") String citySlug, @Param("countrySlug") String countrySlug);

	@Query("SELECT DISTINCT l.countrySlug FROM Memory m JOIN m.location l WHERE m.state=:state AND l.countrySlug IS NOT NULL")
	List<String> findAllCountrySlugsWithPublishedMemories(@Param("state") MemoryState state);

	@Query("SELECT DISTINCT l.citySlug FROM Memory m JOIN m.location l WHERE m.state = :state AND l.countrySlug = :countrySlug AND l.citySlug IS NOT NULL")
	List<String> findAllCitySlugsByCountryWithPublishedMemories(@Param("state") MemoryState state,
			@Param("countrySlug") String countrySlug);

}
