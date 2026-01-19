package fr.memoires_vives.bll;

import java.util.List;
import java.util.Optional;

import fr.memoires_vives.bo.Category;

public interface CategoryService {
	List<Category> getAllCategories();
	Category createCategory(Category category);
	Optional<Category> getCategoryById(long categoryId);
}
