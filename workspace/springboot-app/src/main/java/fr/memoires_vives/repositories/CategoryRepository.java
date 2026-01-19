package fr.memoires_vives.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.memoires_vives.bo.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	Optional<Category> findByCategoryId(long categoryId);
	Optional<Category> findByName(String name);
}
