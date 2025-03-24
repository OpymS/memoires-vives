package fr.memoires_vives.bll;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.memoires_vives.bo.Category;
import fr.memoires_vives.repositories.CategoryRepository;

@Service
public class CategoryServiceImpl implements CategoryService {
	private final CategoryRepository categoryRepository;
	
	public CategoryServiceImpl(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
	
	@Override
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@Override
	public Category createCategory(Category category) {
		return categoryRepository.save(category);
	}

	@Override
	public Category getCategoryById(long categoryId) {
		return categoryRepository.findByCategoryId(categoryId);
	}

		
}
