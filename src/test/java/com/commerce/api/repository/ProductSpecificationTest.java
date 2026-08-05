package com.commerce.api.repository;

import com.commerce.api.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ProductSpecification — Tests de Filtros JPA")
class ProductSpecificationTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll(); // Limpia los datos de data.sql para test isolation
        productRepository.saveAll(List.of(
                new Product("Laptop 100%",        new BigDecimal("1299.99"), 10, 2L, "Electrónica"),
                new Product("Mouse_Gaming Pro",   new BigDecimal("49.99"),   50, 2L, "Electrónica"),
                new Product("Teclado Mecánico",   new BigDecimal("89.99"),   30, 2L, "Electrónica"),
                new Product("Silla Ergonómica",   new BigDecimal("599.99"),   8, 3L, "Muebles"),
                new Product("Camiseta Deportiva", new BigDecimal("29.99"),  100, 1L, "Ropa")
        ));
    }

    // ─── nameContains: escape de comodines SQL ────────────────────────────────

    @Test
    @DisplayName("nameContains '%': el símbolo de porcentaje en el input es literal, no comodín SQL")
    void nameContains_EscapesPercentWildcard_FindsExactMatch() {
        var spec = ProductSpecification.nameContains("100%");
        List<Product> result = productRepository.findAll(spec);

        // Debe encontrar SOLO "Laptop 100%", no todos los productos
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop 100%");
    }

    @Test
    @DisplayName("nameContains '_': el guion bajo en el input es literal, no comodín SQL de un carácter")
    void nameContains_EscapesUnderscoreWildcard_FindsExactMatch() {
        var spec = ProductSpecification.nameContains("Mouse_");
        List<Product> result = productRepository.findAll(spec);

        // Debe encontrar SOLO "Mouse_Gaming Pro", no cualquier producto con 5 letras + 'Gaming Pro'
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mouse_Gaming Pro");
    }

    // ─── nameContains: insensibilidad a mayúsculas ────────────────────────────

    @Test
    @DisplayName("nameContains es case-insensitive (Locale.ROOT)")
    void nameContains_CaseInsensitive_FindsMatch() {
        var spec = ProductSpecification.nameContains("LAPTOP");
        List<Product> result = productRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop 100%");
    }

    @Test
    @DisplayName("nameContains: búsqueda parcial en minúsculas encuentra coincidencias en mayúsculas")
    void nameContains_PartialLower_FindsUpperCaseMatch() {
        var spec = ProductSpecification.nameContains("teclado");
        List<Product> result = productRepository.findAll(spec);

        assertThat(result).hasSize(1);
    }

    // ─── Filtros de rango ────────────────────────────────────────────────────

    @Test
    @DisplayName("priceGreaterThanOrEqual + priceLessThanOrEqual: filtra rango correctamente")
    void buildFilter_PriceRange_FiltersCorrectly() {
        var spec = ProductSpecification.buildFilter(null,
                new BigDecimal("50"), new BigDecimal("100"), null, null, null);
        List<Product> result = productRepository.findAll(spec);

        // Solo Teclado Mecánico (89.99) está en el rango [50, 100]
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Teclado Mecánico");
    }

    @Test
    @DisplayName("categoryIdEquals: filtra por categoria correctamente")
    void buildFilter_CategoryId_FiltersCorrectly() {
        var spec = ProductSpecification.categoryIdEquals(3L);
        List<Product> result = productRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Silla Ergonómica");
    }

    // ─── Combinación de filtros ───────────────────────────────────────────────

    @Test
    @DisplayName("buildFilter: combinación nombre + precio + categoría actúa como AND")
    void buildFilter_CombinedFilters_ActsAsAnd() {
        var spec = ProductSpecification.buildFilter(
                "mouse", new BigDecimal("10"), new BigDecimal("100"), null, null, 2L);
        List<Product> result = productRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mouse_Gaming Pro");
    }

    @Test
    @DisplayName("buildFilter: todos los parámetros null → devuelve todos los productos")
    void buildFilter_AllNull_ReturnsAll() {
        var spec = ProductSpecification.buildFilter(null, null, null, null, null, null);
        List<Product> result = productRepository.findAll(spec);

        assertThat(result).hasSize(5);
    }

    // ─── Filtros de stock ────────────────────────────────────────────────────

    @Test
    @DisplayName("stockGreaterThanOrEqual + stockLessThanOrEqual: filtra rango de stock")
    void buildFilter_StockRange_FiltersCorrectly() {
        var spec = ProductSpecification.buildFilter(null, null, null, 20, 60, null);
        List<Product> result = productRepository.findAll(spec);

        // Teclado (30) y Mouse (50) están en [20, 60]
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("Mouse_Gaming Pro", "Teclado Mecánico");
    }
}
