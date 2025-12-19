package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Object> findByCategoryName(String categoryName);
}
