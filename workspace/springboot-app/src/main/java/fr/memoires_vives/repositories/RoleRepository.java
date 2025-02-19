package fr.memoires_vives.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.memoires_vives.bo.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Role findByName(String name);
}
