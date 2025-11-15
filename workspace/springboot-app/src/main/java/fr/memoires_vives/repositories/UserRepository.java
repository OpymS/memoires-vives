package fr.memoires_vives.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.memoires_vives.bo.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByPseudo(String pseudo);
	Optional<User> findByUserId(long userId);
	Optional<User> findByEmail(String email);
	
	@Query("SELECT u.password FROM User u WHERE u.userId = :userId")
	String findPasswordByUserId(@Param("userId") long userId);
	
	@Query("SELECT u FROM User u LEFT JOIN FETCH u.friends WHERE u.pseudo = :pseudo")
	User findByPseudoWithFriends(@Param("pseudo") String pseudo);
}
