package com.commerce.api.dto.mapper;

import com.commerce.api.dto.request.ProductRequest;
import com.commerce.api.dto.response.ProductResponse;
import com.commerce.api.entity.Product;

public class ProductMapper {

    private ProductMapper() {}

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getCategoryId(),
                product.getCategoryName()
        );
    }

    public static Product toEntity(ProductRequest request, String categoryName) {
        return new Product(
                request.name(),
                request.price(),
                request.stock(),
                request.categoryId(),
                categoryName
        );
    }
}
