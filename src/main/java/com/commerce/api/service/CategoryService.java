package com.commerce.api.service;

import com.commerce.api.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> fetchAllCategories();
    CategoryResponse fetchCategoryById(Long id);
}
