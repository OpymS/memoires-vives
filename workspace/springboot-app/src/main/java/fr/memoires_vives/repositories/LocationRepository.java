package fr.memoires_vives.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.memoires_vives.bo.Location;

public interface LocationRepository  extends JpaRepository<Location, Long>{
	Location findByLocationId(long locationId);
}
