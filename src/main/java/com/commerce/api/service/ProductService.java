package com.commerce.api.service;

import com.commerce.api.dto.request.ProductRequest;
import com.commerce.api.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    Page<ProductResponse> getProducts(String name, BigDecimal minPrice, BigDecimal maxPrice,
                                      Integer minStock, Integer maxStock, Long categoryId,
                                      Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}
