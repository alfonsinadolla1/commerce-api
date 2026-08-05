package com.commerce.api.exception;

import com.commerce.api.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests focalizados en GlobalExceptionHandler, usando ProductController como punto de entrada.
 * Verificamos que los handlers traduzcan correctamente las excepciones a ErrorResponse con
 * el código HTTP correcto y los campos esperados.
 */
@WebMvcTest(controllers = com.commerce.api.controller.ProductController.class)
@DisplayName("GlobalExceptionHandler — Tests de Mapeo de Errores")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("POST con Content-Type: text/plain → 415 con mensaje en español")
    void post_TextPlainContentType_Returns415() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=Laptop&price=999"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("text/plain")))
                .andExpect(jsonPath("$.path").value("/products"));
    }

    @Test
    @DisplayName("POST con JSON malformado → 400 con mensaje genérico (sin filtrar internals)")
    void post_MalformedJson_Returns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cuerpo de la petición inválido o mal formado"));
    }

    @Test
    @DisplayName("POST con campos de validación inválidos → 400 con lista de errores por campo")
    void post_ValidationErrors_Returns400WithFieldErrors() throws Exception {
        String body = """
                {"name":"","price":-1.00,"stock":-5,"categoryId":null}
                """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @DisplayName("GET /products/abc (ID no numérico) → 400 con mención del parámetro")
    void get_NonNumericId_Returns400() throws Exception {
        mockMvc.perform(get("/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("id")));
    }

    @Test
    @DisplayName("PATCH (método no soportado) → 405 con mención del método")
    void patch_MethodNotSupported_Returns405() throws Exception {
        mockMvc.perform(patch("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("PATCH")));
    }

    @Test
    @DisplayName("GET /ruta-inexistente → 404 con mensaje de recurso no encontrado")
    void get_NonExistentRoute_Returns404() throws Exception {
        mockMvc.perform(get("/ruta-que-no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
