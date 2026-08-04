package com.commerce.api.service;

import com.commerce.api.client.CategoryApiClient;
import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryApiClient categoryApiClient;

    public CategoryServiceImpl(CategoryApiClient categoryApiClient) {
        this.categoryApiClient = categoryApiClient;
    }

    @Override
    @Cacheable("categories")
    public List<CategoryResponse> fetchAllCategories() {
        return categoryApiClient.fetchCategories();
    }

    @Override
    public CategoryResponse fetchCategoryById(Long id) {
        return fetchAllCategories().stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
