package fr.memoires_vives.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.memoires_vives.bo.User;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByPseudo(String pseudo);
}
