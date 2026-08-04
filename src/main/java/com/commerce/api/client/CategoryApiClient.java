package com.commerce.api.client;

import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.exception.ExternalApiException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Arrays;
import java.util.List;

@Component
public class CategoryApiClient {

    private final RestClient restClient;

    public CategoryApiClient(RestClient categoryRestClient) {
        this.restClient = categoryRestClient;
    }

    public List<CategoryResponse> fetchCategories() {
        try {
            CategoryResponse[] response = restClient.get()
                    .uri("/categories")
                    .retrieve()
                    .body(CategoryResponse[].class);

            return response != null ? Arrays.asList(response) : List.of();

        } catch (RestClientResponseException ex) {
            throw new ExternalApiException(
                    "Error en la API externa de categorías [HTTP %d]: %s"
                            .formatted(ex.getStatusCode().value(), ex.getMessage()),
                    ex
            );
        } catch (ResourceAccessException ex) {
            throw new ExternalApiException(
                    "No se pudo conectar a la API externa de categorías: " + ex.getMessage(),
                    ex
            );
        }
    }
}
