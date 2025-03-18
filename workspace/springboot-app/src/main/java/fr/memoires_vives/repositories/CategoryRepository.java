package fr.memoires_vives.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.memoires_vives.bo.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	Category findByCategoryId(long categoryId);
	Category findByName(String name);
}
