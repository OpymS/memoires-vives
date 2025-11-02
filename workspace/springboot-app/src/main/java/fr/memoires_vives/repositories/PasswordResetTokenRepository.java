package fr.memoires_vives.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.memoires_vives.bo.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
	Optional<PasswordResetToken> findByToken(String token);
    void deleteByToken(String token);
}
