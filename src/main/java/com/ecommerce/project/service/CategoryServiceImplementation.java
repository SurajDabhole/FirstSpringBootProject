package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.NoCategoriesFoundException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImplementation implements CategoryService {
//    private List<Category> categories = new ArrayList<>();

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        long totalCategories = categoryRepository.count();

        if (totalCategories == 0) {
            throw new NoCategoriesFoundException("No categories found in the database.");
        }
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        String categoryName = category.getCategoryName();

        // Check if category with the same name already exists
        categoryRepository.findByCategoryName(categoryName)
                .ifPresent(existingCategory -> {
                    throw new APIException("Category with name '" + categoryName + "' already exists");
                });

        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId, "Category"));

        categoryRepository.delete(category);
        return "Category deleted successfully.";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId, "Category"));

        String categoryName = category.getCategoryName();

        // Check if category with the same name already exists
        categoryRepository.findByCategoryName(categoryName)
                .ifPresent(existingCategory -> {
                    throw new APIException("Category with name '" + categoryName + "' already exists");
                });

        savedCategory.setCategoryName(category.getCategoryName());
        return categoryRepository.save(savedCategory);
    }
}
