package com.ecommerce.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "categories")
@Data   // generates getters and setters and toString, equals, and hashCode methods
@NoArgsConstructor  // generates a no-argument constructor
@AllArgsConstructor // generates a constructor where all fields are parameters and they are initialized
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank
    @Size(min = 2, message = "Category name must be at least 2 characters long")
    private String categoryName;
}
