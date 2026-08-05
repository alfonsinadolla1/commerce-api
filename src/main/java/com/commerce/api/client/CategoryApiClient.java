package com.commerce.api.client;

import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.List;

@Component
public class CategoryApiClient {

    private static final Logger log = LoggerFactory.getLogger(CategoryApiClient.class);
    private static final String CLEAN_ERROR_MSG = "El servicio de categorías no está disponible temporalmente";

    private final RestClient restClient;

    public CategoryApiClient(RestClient categoryRestClient) {
        this.restClient = categoryRestClient;
    }

    public List<CategoryResponse> fetchCategories() {
        log.info("Consultando API externa de categorías...");
        try {
            CategoryResponse[] response = restClient.get()
                    .uri("/categories")
                    .retrieve()
                    .body(CategoryResponse[].class);

            List<CategoryResponse> categories = response != null ? Arrays.asList(response) : List.of();
            log.info("API externa respondió con {} categorías.", categories.size());
            return categories;

        } catch (RestClientResponseException ex) {
            log.warn("Error HTTP [{}] al consultar categorías externas.", ex.getStatusCode().value());
            log.debug("Detalle del error HTTP:", ex);
            throw new ExternalApiException(CLEAN_ERROR_MSG, ex);

        } catch (ResourceAccessException ex) {
            log.warn("Error de red/timeout al consultar categorías externas: {}", ex.getMessage());
            log.debug("Detalle del error de red:", ex);
            throw new ExternalApiException(CLEAN_ERROR_MSG, ex);

        } catch (RestClientException ex) {
            // Captura cualquier otro fallo del cliente HTTP (respuesta malformada, etc.)
            log.warn("Error inesperado del cliente HTTP al consultar categorías: {}", ex.getMessage());
            log.debug("Detalle del error inesperado:", ex);
            throw new ExternalApiException(CLEAN_ERROR_MSG, ex);
        }
    }
}
