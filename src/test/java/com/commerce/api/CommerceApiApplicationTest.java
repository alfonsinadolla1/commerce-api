package com.commerce.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración de la aplicación completa contra H2 en memoria.
 * Verifica que el contexto Spring Boot arranque correctamente y que los
 * endpoints funcionen de extremo a extremo con datos del seed (data.sql).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CommerceApi — Tests de Integración (Smoke + Sorters)")
class CommerceApiApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("contextLoads: el contexto de Spring Boot arranca correctamente con H2")
    void contextLoads() {
        // Si el contexto no arranca, el test falla automáticamente.
        // Verifica que todos los beans (RestClient, CacheManager, JPA) se configuran sin error.
    }

    @Test
    @DisplayName("GET /products: devuelve 200 con los 8 productos del seed (data.sql)")
    void getProducts_ReturnsSeedData_200() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(8))
                .andExpect(jsonPath("$.content[0].categoryName").isString());
    }

    @Test
    @DisplayName("GET /products?sort=campoInexistente → 400 (PropertyReferenceException → GlobalExceptionHandler)")
    void getProducts_InvalidSort_Returns400() throws Exception {
        mockMvc.perform(get("/products").param("sort", "campoInexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("campoInexistente")));
    }

    @Test
    @DisplayName("GET /products/{id}: producto existente del seed → 200")
    void getProductById_FromSeed_Returns200() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.categoryName").isString());
    }

    @Test
    @DisplayName("GET /products/{id}: ID inexistente → 404")
    void getProductById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/products/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No se encontró Producto con id: 9999"));
    }
}
