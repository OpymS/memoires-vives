package fr.memoires_vives.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.memoires_vives.bo.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
	Location findByLocationId(long locationId);

	@Query("SELECT l FROM Location l WHERE (l.latitude <= :north AND l.latitude >= :south) " + 
	"AND ((:west < :east AND l.longitude <= :east AND l.longitude >= :west) "+
			"OR (:west > :east AND (l.longitude <= :east OR l.longitude >= :west)))")
	List<Location> findInSquare(@Param("north") double north, @Param("south") double south, @Param("east") double east,
			@Param("west") double west);
}
