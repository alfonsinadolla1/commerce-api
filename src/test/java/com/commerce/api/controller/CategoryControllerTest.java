package com.commerce.api.controller;

import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.exception.ExternalApiException;
import com.commerce.api.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController — Tests de Capa Web")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("GET /categories: API externa disponible → 200 OK con lista de categorías")
    void getCategories_Returns200() throws Exception {
        List<CategoryResponse> categories = List.of(
                new CategoryResponse(1L, "Ropa"),
                new CategoryResponse(2L, "Electrónica"),
                new CategoryResponse(3L, "Muebles")
        );
        given(categoryService.fetchAllCategories()).willReturn(categories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Ropa"))
                .andExpect(jsonPath("$[1].name").value("Electrónica"));
    }

    @Test
    @DisplayName("GET /categories: API externa falla sin caché → 502 Bad Gateway")
    void getCategories_ExternalApiFailure_Returns502() throws Exception {
        given(categoryService.fetchAllCategories())
                .willThrow(new ExternalApiException(
                        "El servicio de categorías no está disponible temporalmente",
                        new RuntimeException("Connection refused")));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message")
                        .value("El servicio de categorías no está disponible temporalmente"));
    }

    @Test
    @DisplayName("GET /categories: caché tiene datos stale → 200 OK (stale-while-revalidate)")
    void getCategories_StaleCache_Returns200() throws Exception {
        // Simula el fallback stale: el service devuelve datos cacheados incluso si la API falló
        List<CategoryResponse> staleCategories = List.of(
                new CategoryResponse(1L, "Ropa (stale)"),
                new CategoryResponse(2L, "Electrónica (stale)")
        );
        given(categoryService.fetchAllCategories()).willReturn(staleCategories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
