package com.commerce.api.service;

import com.commerce.api.dto.mapper.ProductMapper;
import com.commerce.api.dto.request.ProductRequest;
import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.dto.response.ProductResponse;
import com.commerce.api.entity.Product;
import com.commerce.api.exception.ResourceNotFoundException;
import com.commerce.api.repository.ProductRepository;
import com.commerce.api.repository.ProductSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

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
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        return ProductMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        CategoryResponse category = categoryService.fetchCategoryById(request.categoryId());
        Product product = ProductMapper.toEntity(request, category.name());
        ProductResponse response = ProductMapper.toResponse(productRepository.save(product));
        log.info("Producto creado con id={}, nombre='{}'", response.id(), response.name());
        return response;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));

        CategoryResponse category = categoryService.fetchCategoryById(request.categoryId());

        product.setName(request.name());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategoryId(request.categoryId());
        product.setCategoryName(category.name());

        // La entidad está en estado "managed": Hibernate persiste los cambios
        // automáticamente al finalizar la transacción (dirty checking). No se
        // necesita llamar a save() explícitamente.
        log.info("Producto actualizado id={}, nombre='{}'", id, request.name());
        return ProductMapper.toResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        productRepository.delete(product);
        log.info("Producto eliminado id={}", id);
    }
}
