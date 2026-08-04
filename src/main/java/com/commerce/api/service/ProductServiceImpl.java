package com.commerce.api.service;

import com.commerce.api.dto.mapper.ProductMapper;
import com.commerce.api.dto.request.ProductRequest;
import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.dto.response.ProductResponse;
import com.commerce.api.entity.Product;
import com.commerce.api.exception.ResourceNotFoundException;
import com.commerce.api.repository.ProductRepository;
import com.commerce.api.repository.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductServiceImpl(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Override
    public Page<ProductResponse> getProducts(
            String name, BigDecimal minPrice, BigDecimal maxPrice,
            Integer minStock, Integer maxStock, Long categoryId,
            Pageable pageable) {

        Specification<Product> spec = ProductSpecification.buildFilter(
                name, minPrice, maxPrice, minStock, maxStock, categoryId);

        return productRepository.findAll(spec, pageable)
                .map(ProductMapper::toResponse);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return ProductMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        CategoryResponse category = categoryService.fetchCategoryById(request.categoryId());
        Product product = ProductMapper.toEntity(request, category.name());
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        CategoryResponse category = categoryService.fetchCategoryById(request.categoryId());

        product.setName(request.name());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategoryId(request.categoryId());
        product.setCategoryName(category.name());

        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }
}
