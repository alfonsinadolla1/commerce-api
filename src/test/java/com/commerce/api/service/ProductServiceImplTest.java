package com.commerce.api.service;

import com.commerce.api.dto.request.ProductRequest;
import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.dto.response.ProductResponse;
import com.commerce.api.entity.Product;
import com.commerce.api.exception.ResourceNotFoundException;
import com.commerce.api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl — Tests Unitarios")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private CategoryResponse categoryResponse;
    private Product savedProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        categoryResponse = new CategoryResponse(1L, "Updated Category Name");

        productRequest = new ProductRequest(
                "Laptop Pro",
                new BigDecimal("1299.99"),
                25,
                1L
        );

        savedProduct = new Product(
                "Laptop Pro",
                new BigDecimal("1299.99"),
                25,
                1L,
                "Updated Category Name"
        );
    }

    // ─── createProduct ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createProduct: categoría válida → persiste y retorna ProductResponse correcto")
    void createProduct_Successful() {
        given(categoryService.fetchCategoryById(1L)).willReturn(categoryResponse);
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        ProductResponse result = productService.createProduct(productRequest);

        assertThat(result.name()).isEqualTo("Laptop Pro");
        assertThat(result.price()).isEqualByComparingTo("1299.99");
        assertThat(result.stock()).isEqualTo(25);
        assertThat(result.categoryId()).isEqualTo(1L);
        assertThat(result.categoryName()).isEqualTo("Updated Category Name");

        then(categoryService).should().fetchCategoryById(1L);
        then(productRepository).should().save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct: categoría inexistente → lanza ResourceNotFoundException, no persiste")
    void createProduct_CategoryNotFound_ThrowsException() {
        given(categoryService.fetchCategoryById(99L))
                .willThrow(new ResourceNotFoundException("Categoría", 99L));

        ProductRequest requestConCategoriaInvalida = new ProductRequest(
                "Laptop Pro", new BigDecimal("1299.99"), 25, 99L
        );

        assertThatThrownBy(() -> productService.createProduct(requestConCategoriaInvalida))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoría")
                .hasMessageContaining("99");

        then(productRepository).should(never()).save(any(Product.class));
    }

    // ─── getProductById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getProductById: ID existente → retorna ProductResponse correcto")
    void getProductById_Successful() {
        given(productRepository.findById(1L)).willReturn(Optional.of(savedProduct));

        ProductResponse result = productService.getProductById(1L);

        assertThat(result.name()).isEqualTo("Laptop Pro");
        assertThat(result.categoryName()).isEqualTo("Updated Category Name");
    }

    @Test
    @DisplayName("getProductById: ID inexistente → lanza ResourceNotFoundException con mensaje en español")
    void getProductById_NotFound_ThrowsException() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto")
                .hasMessageContaining("999");
    }

    // ─── deleteProduct ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct: ID existente → invoca delete(entity) una vez")
    void deleteProduct_Successful() {
        // deleteProduct ahora usa findById + delete(entity), NO existsById + deleteById
        given(productRepository.findById(1L)).willReturn(Optional.of(savedProduct));

        productService.deleteProduct(1L);

        then(productRepository).should().delete(savedProduct);
        then(productRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteProduct: ID inexistente → lanza ResourceNotFoundException, no llama delete")
    void deleteProduct_NotFound_ThrowsException() {
        given(productRepository.findById(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto")
                .hasMessageContaining("404");

        then(productRepository).should(never()).delete(any(Product.class));
    }

    // ─── updateProduct ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProduct: ID existente y categoría válida → actualiza campos y retorna sin llamar save()")
    void updateProduct_Successful() {
        given(productRepository.findById(1L)).willReturn(Optional.of(savedProduct));
        given(categoryService.fetchCategoryById(2L))
                .willReturn(new CategoryResponse(2L, "Updated Category Name1"));

        ProductRequest updateRequest = new ProductRequest(
                "Laptop Pro v2", new BigDecimal("999.99"), 10, 2L
        );

        ProductResponse result = productService.updateProduct(1L, updateRequest);

        assertThat(result.name()).isEqualTo("Laptop Pro v2");
        assertThat(result.price()).isEqualByComparingTo("999.99");
        assertThat(result.categoryId()).isEqualTo(2L);
        assertThat(result.categoryName()).isEqualTo("Updated Category Name1");

        // updateProduct ya no llama save() — entidad managed, Hibernate la persiste en flush
        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct: ID inexistente → lanza ResourceNotFoundException")
    void updateProduct_NotFound_ThrowsException() {
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(999L, productRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto")
                .hasMessageContaining("999");
    }
}
