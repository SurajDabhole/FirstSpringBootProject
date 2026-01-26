package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String productName;

    private String image;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @Min(value = 0, message = "Quantity must be at least 0")
    private Integer quantity;

    @Min(value = 0, message = "Price must be at least 0.0")
    private double price;

    @Min(value = 0, message = "Discount must be at least 0.0")
    @Max(value = 100, message = "Discount cannot exceed 100.0")
    private double discount;

    private Double specialPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
