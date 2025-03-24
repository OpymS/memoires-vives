package fr.memoires_vives.bll;

import java.util.List;

import fr.memoires_vives.bo.Category;

public interface CategoryService {
	List<Category> getAllCategories();
	Category createCategory(Category category);
	Category getCategoryById(long categoryId);
}
