package com.commerce.api.repository;

import com.commerce.api.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) ->
                name == null ? null
                        : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null
                        : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null
                        : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> stockGreaterThanOrEqual(Integer minStock) {
        return (root, query, cb) ->
                minStock == null ? null
                        : cb.greaterThanOrEqualTo(root.get("stock"), minStock);
    }

    public static Specification<Product> stockLessThanOrEqual(Integer maxStock) {
        return (root, query, cb) ->
                maxStock == null ? null
                        : cb.lessThanOrEqualTo(root.get("stock"), maxStock);
    }

    public static Specification<Product> categoryIdEquals(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null
                        : cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<Product> buildFilter(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minStock,
            Integer maxStock,
            Long categoryId
    ) {
        return Specification
                .where(nameContains(name))
                .and(priceGreaterThanOrEqual(minPrice))
                .and(priceLessThanOrEqual(maxPrice))
                .and(stockGreaterThanOrEqual(minStock))
                .and(stockLessThanOrEqual(maxStock))
                .and(categoryIdEquals(categoryId));
    }
}
