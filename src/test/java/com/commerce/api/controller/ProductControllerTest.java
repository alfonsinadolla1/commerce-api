package com.commerce.api.controller;

import com.commerce.api.dto.request.ProductRequest;
import com.commerce.api.dto.response.ProductResponse;
import com.commerce.api.exception.ExternalApiException;
import com.commerce.api.exception.ResourceNotFoundException;
import com.commerce.api.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController — Tests de Capa Web")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private static final ProductResponse PRODUCT_RESPONSE = new ProductResponse(
            1L, "Laptop Pro 15", new BigDecimal("1299.99"), 25, 2L, "Electrónica"
    );

    // ─── GET /products ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /products → 200 OK con página de productos")
    void getProducts_Returns200WithPage() throws Exception {
        given(productService.getProducts(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(PRODUCT_RESPONSE)));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Laptop Pro 15"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /products con filtros → 200 OK (delega filtros al service)")
    void getProducts_WithFilters_Returns200() throws Exception {
        given(productService.getProducts(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/products")
                        .param("name", "laptop")
                        .param("minPrice", "100")
                        .param("maxPrice", "2000")
                        .param("categoryId", "2")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ─── GET /products/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /products/{id}: ID existente → 200 OK")
    void getProductById_Returns200() throws Exception {
        given(productService.getProductById(1L)).willReturn(PRODUCT_RESPONSE);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Pro 15"))
                .andExpect(jsonPath("$.categoryName").value("Electrónica"));
    }

    @Test
    @DisplayName("GET /products/{id}: ID inexistente → 404 Not Found")
    void getProductById_NotFound_Returns404() throws Exception {
        given(productService.getProductById(999L))
                .willThrow(new ResourceNotFoundException("Producto", 999L));

        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No se encontró Producto con id: 999"));
    }

    @Test
    @DisplayName("GET /products/abc: ID no numérico → 400 Bad Request")
    void getProductById_InvalidIdType_Returns400() throws Exception {
        mockMvc.perform(get("/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─── POST /products ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /products: body válido → 201 Created")
    void createProduct_Returns201() throws Exception {
        ProductRequest request = new ProductRequest("Laptop Pro 15", new BigDecimal("1299.99"), 25, 2L);
        given(productService.createProduct(any(ProductRequest.class))).willReturn(PRODUCT_RESPONSE);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Pro 15"));
    }

    @Test
    @DisplayName("POST /products: JSON malformado → 400 Bad Request")
    void createProduct_MalformedJson_Returns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cuerpo de la petición inválido o mal formado"));
    }

    @Test
    @DisplayName("POST /products: validación falla (nombre vacío, precio negativo) → 400")
    void createProduct_ValidationFails_Returns400() throws Exception {
        String invalidBody = """
                {"name":"","price":-1.00,"stock":-5,"categoryId":null}
                """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /products con Content-Type: text/plain → 415 Unsupported Media Type")
    void createProduct_WrongContentType_Returns415() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    @DisplayName("POST /products: API externa de categorías falla → 502 Bad Gateway")
    void createProduct_ExternalApiFails_Returns502() throws Exception {
        given(productService.createProduct(any()))
                .willThrow(new ExternalApiException("El servicio de categorías no está disponible temporalmente",
                        new RuntimeException("connection refused")));

        String validBody = """
                {"name":"Laptop","price":999.99,"stock":5,"categoryId":1}
                """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }

    // ─── PUT /products/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /products/{id}: body válido → 200 OK")
    void updateProduct_Returns200() throws Exception {
        ProductRequest request = new ProductRequest("Laptop v2", new BigDecimal("999.99"), 10, 2L);
        given(productService.updateProduct(eq(1L), any(ProductRequest.class))).willReturn(PRODUCT_RESPONSE);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /products/{id}: ID inexistente → 404 Not Found")
    void updateProduct_NotFound_Returns404() throws Exception {
        given(productService.updateProduct(eq(999L), any()))
                .willThrow(new ResourceNotFoundException("Producto", 999L));

        String body = "{\"name\":\"X\",\"price\":10.00,\"stock\":1,\"categoryId\":1}";

        mockMvc.perform(put("/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /products/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /products/{id}: ID existente → 204 No Content")
    void deleteProduct_Returns204() throws Exception {
        willDoNothing().given(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /products/{id}: ID inexistente → 404 Not Found")
    void deleteProduct_NotFound_Returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Producto", 999L))
                .when(productService).deleteProduct(999L);

        mockMvc.perform(delete("/products/999"))
                .andExpect(status().isNotFound());
    }

    // ─── Método HTTP no soportado ─────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /products/{id} → 405 Method Not Allowed")
    void patchProduct_Returns405() throws Exception {
        mockMvc.perform(patch("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}
