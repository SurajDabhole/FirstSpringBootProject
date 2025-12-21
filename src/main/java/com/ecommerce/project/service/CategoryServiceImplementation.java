package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.NoCategoriesFoundException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImplementation implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryServiceImplementation(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Create Sort object based on sortBy field and sortOrder (ASC/DESC)
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Create Pageable with page number, page size, and sorting
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Fetch paginated and sorted categories from database
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<Category> categoryList = categoryPage.getContent();
        if (categoryList.isEmpty()) {
            throw new NoCategoriesFoundException("No categories found in the database.");
        }

        // Convert Category entities to CategoryDTOs
        List<CategoryDTO> categoryDTOS = categoryList.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        // Build and populate the response
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());

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
                .orElseThrow(() -> new ResourceNotFoundException("categoryId", categoryId, "Category"));

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
                .orElseThrow(() -> new ResourceNotFoundException("categoryId", categoryId, "Category"));

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
