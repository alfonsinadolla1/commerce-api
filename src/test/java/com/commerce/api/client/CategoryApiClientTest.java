package com.commerce.api.client;

import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.exception.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Test de integración del cliente HTTP CategoryApiClient.
 *
 * Estrategia: RestClient.builder(RestTemplate) delega las llamadas HTTP al
 * RestTemplate subyacente. MockRestServiceServer reemplaza el
 * ClientHttpRequestFactory del RestTemplate con un stub que intercepta las
 * requests y retorna respuestas configuradas, sin levantar ningún servidor real.
 *
 * CategoryApiClient ya NO tiene @Cacheable; el caché es responsabilidad de
 * CategoryServiceImpl. Estos tests verifican únicamente la lógica HTTP.
 */
@DisplayName("CategoryApiClient — Tests de Integración HTTP")
class CategoryApiClientTest {

    private static final String BASE_URL = "https://api.escuelajs.co/api/v1";
    private static final String CATEGORIES_URL = BASE_URL + "/categories";

    private MockRestServiceServer mockServer;
    private CategoryApiClient categoryApiClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestClient restClient = RestClient.builder(restTemplate)
                .baseUrl(BASE_URL)
                .build();
        categoryApiClient = new CategoryApiClient(restClient);
    }

    // ─── Respuesta exitosa ────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchCategories: API responde 200 → deserializa lista correctamente")
    void fetchCategories_Successful() {
        String json = """
                [
                  {"id": 1, "name": "Updated Category Name"},
                  {"id": 2, "name": "Updated Category Name1"},
                  {"id": 3, "name": "Furniture"}
                ]
                """;

        mockServer.expect(requestTo(CATEGORIES_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<CategoryResponse> result = categoryApiClient.fetchCategories();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Updated Category Name");
        assertThat(result.get(2).name()).isEqualTo("Furniture");

        mockServer.verify();
    }

    @Test
    @DisplayName("fetchCategories: API incluye campos extra → @JsonIgnoreProperties los descarta sin error")
    void fetchCategories_WithExtraFields_Successful() {
        String json = """
                [
                  {
                    "id": 5,
                    "name": "Miscellaneous",
                    "image": "https://example.com/img.jpg",
                    "creationAt": "2024-01-01T00:00:00.000Z",
                    "updatedAt": "2024-06-01T00:00:00.000Z"
                  }
                ]
                """;

        mockServer.expect(requestTo(CATEGORIES_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<CategoryResponse> result = categoryApiClient.fetchCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(5L);
        assertThat(result.get(0).name()).isEqualTo("Miscellaneous");

        mockServer.verify();
    }

    // ─── Errores HTTP de servidor (5xx) ──────────────────────────────────────

    @Test
    @DisplayName("fetchCategories: API responde 500 → lanza ExternalApiException con código HTTP en el mensaje")
    void fetchCategories_ServerError500_ThrowsExternalApiException() {
        mockServer.expect(requestTo(CATEGORIES_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> categoryApiClient.fetchCategories())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("categorías no está disponible");

        mockServer.verify();
    }

    @Test
    @DisplayName("fetchCategories: API responde 503 → lanza ExternalApiException")
    void fetchCategories_ServiceUnavailable503_ThrowsExternalApiException() {
        mockServer.expect(requestTo(CATEGORIES_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> categoryApiClient.fetchCategories())
                .isInstanceOf(ExternalApiException.class);

        mockServer.verify();
    }

    // ─── Errores HTTP de cliente (4xx) ───────────────────────────────────────

    @Test
    @DisplayName("fetchCategories: API responde 404 → lanza ExternalApiException con código HTTP en el mensaje")
    void fetchCategories_ClientError404_ThrowsExternalApiException() {
        mockServer.expect(requestTo(CATEGORIES_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> categoryApiClient.fetchCategories())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("categorías no está disponible");

        mockServer.verify();
    }

    @Test
    @DisplayName("fetchCategories: API responde 401 → lanza ExternalApiException")
    void fetchCategories_Unauthorized401_ThrowsExternalApiException() {
        mockServer.expect(requestTo(CATEGORIES_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> categoryApiClient.fetchCategories())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("categorías no está disponible");

        mockServer.verify();
    }
}
