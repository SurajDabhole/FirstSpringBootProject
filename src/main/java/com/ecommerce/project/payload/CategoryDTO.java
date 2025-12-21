package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {  // Representing Category model at presentation layer (client -> server => converts received data)
    private Long categoryId;
    private String categoryName;
}
