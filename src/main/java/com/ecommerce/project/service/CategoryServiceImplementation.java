package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.NoCategoriesFoundException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImplementation implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories() {
        long totalCategories = categoryRepository.count();
        List<Category> categories = categoryRepository.findAll();

        if (totalCategories == 0) {
            throw new NoCategoriesFoundException("No categories found in the database.");
        }

        List<CategoryDTO> categoryDTOS = categories.stream().
                map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);

        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        String categoryName = categoryDTO.getCategoryName();

        // Check if category with the same name already exists
        categoryRepository.findByCategoryName(categoryName)
                .ifPresent(_ -> {
                    throw new APIException("Category with name '" + categoryName + "' already exists");
                });

        // Convert CategoryDTO to Category entity
        Category category = modelMapper.map(categoryDTO, Category.class);

        // Save the category entity
        Category savedCategory = categoryRepository.save(category);

        // Convert saved Category entity back to CategoryDTO and return
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId, "Category"));

        // Convert Category entity to CategoryDTO before deleting
        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);

        // Delete the category entity from database
        categoryRepository.delete(category);

        // Return the DTO of the deleted category
        return categoryDTO;
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        // Find the existing category by ID
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId, "Category"));

        // Get the new category name from the input DTO
        String newCategoryName = categoryDTO.getCategoryName();

        // Check if a DIFFERENT category with the same name already exists
        // (We need to exclude the current category being updated)
        categoryRepository.findByCategoryName(newCategoryName)
                .ifPresent(existingCategory -> {
                    // Only throw exception if it's a different category (different ID)
                    if (!existingCategory.getCategoryId().equals(categoryId)) {
                        throw new APIException("Category with name '" + newCategoryName + "' already exists");
                    }
                });

        // Update the category name from the DTO
        savedCategory.setCategoryName(newCategoryName);

        // Save the updated category entity
        Category updatedCategory = categoryRepository.save(savedCategory);

        // Convert and return the updated entity as DTO
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }
}
