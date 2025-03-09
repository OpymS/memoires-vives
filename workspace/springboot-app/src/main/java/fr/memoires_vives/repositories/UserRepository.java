package fr.memoires_vives.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.memoires_vives.bo.User;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByPseudo(String pseudo);
	User findByUserId(long userId);
	User findByEmail(String email);
	
	@Query("SELECT u FROM User u LEFT JOIN FETCH u.friends WHERE u.pseudo = :pseudo")
	User findByPseudoWithFriends(@Param("pseudo") String pseudo);
}
