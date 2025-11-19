package fr.memoires_vives.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import fr.memoires_vives.bo.Token;
import fr.memoires_vives.bo.User;

public interface TokenRepository extends JpaRepository<Token, Long> {
	Optional<Token> findByToken(String token);
    void deleteByToken(String token);
    
    @Modifying
    @Transactional
    void deleteAllByUser(User user);
}
