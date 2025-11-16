package fr.memoires_vives.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.memoires_vives.bo.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
	Optional<Token> findByToken(String token);
    void deleteByToken(String token);
}
