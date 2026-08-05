package com.commerce.api.service;

import com.commerce.api.client.CategoryApiClient;
import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.exception.ExternalApiException;
import com.commerce.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl — Tests Unitarios (Read-Through Cache)")
class CategoryServiceImplTest {

    @Mock
    private CategoryApiClient categoryApiClient;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private CategoryServiceImpl categoryService;

    private final List<CategoryResponse> apiCategories = List.of(
            new CategoryResponse(1L, "Ropa"),
            new CategoryResponse(2L, "Electrónica"),
            new CategoryResponse(3L, "Muebles")
    );

    @BeforeEach
    void setUp() {
        given(cacheManager.getCache("categories")).willReturn(cache);
        categoryService = new CategoryServiceImpl(categoryApiClient, cacheManager);
    }

    // ─── (a) Caché caliente: no llama al cliente HTTP ──────────────────────────

    @Test
    @DisplayName("(a) Caché caliente → devuelve desde caché SIN llamar a la API")
    void fetchAllCategories_CacheHit_DoesNotCallApiClient() {
        given(cache.get(any(), eq(List.class))).willReturn(apiCategories);

        List<CategoryResponse> result = categoryService.fetchAllCategories();

        assertThat(result).hasSize(3);
        then(categoryApiClient).should(never()).fetchCategories();
    }

    // ─── (b) Caché frío: llama a la API y cachea el resultado ────────────────

    @Test
    @DisplayName("(b) Caché frío → llama a la API, guarda en caché y devuelve lista")
    void fetchAllCategories_CacheMiss_CallsApiAndCachesResult() {
        given(cache.get(any(), eq(List.class))).willReturn(null); // cache miss
        given(categoryApiClient.fetchCategories()).willReturn(apiCategories);

        List<CategoryResponse> result = categoryService.fetchAllCategories();

        assertThat(result).hasSize(3);
        then(categoryApiClient).should(times(1)).fetchCategories();
        then(cache).should().put(SimpleKey.EMPTY, apiCategories); // se cachea
    }

    @Test
    @DisplayName("(b2) API devuelve lista vacía → NO se cachea (evita bloquear fallback)")
    void fetchAllCategories_EmptyResponseFromApi_NotCached() {
        given(cache.get(any(), eq(List.class))).willReturn(null);
        given(categoryApiClient.fetchCategories()).willReturn(List.of());

        List<CategoryResponse> result = categoryService.fetchAllCategories();

        assertThat(result).isEmpty();
        then(cache).should(never()).put(any(), any()); // lista vacía NO se cachea
    }

    // ─── (c) API falla con caché stale: sirve el fallback ────────────────────

    @Test
    @DisplayName("(c) API falla y hay datos stale en caché → devuelve stale (stale-while-revalidate)")
    @SuppressWarnings("unchecked")
    void fetchAllCategories_ApiFailsWithStaleCache_ReturnsStale() {
        // Primera llamada (read-through): cache miss
        // Segunda llamada (fallback stale en catch): cache tiene datos
        doReturn(null).doReturn(apiCategories)
                .when(cache).get(any(), eq(List.class));

        given(categoryApiClient.fetchCategories())
                .willThrow(new ExternalApiException("API no disponible"));

        List<CategoryResponse> result = categoryService.fetchAllCategories();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("Ropa");
    }

    // ─── (d) API falla sin caché: relanza ExternalApiException ───────────────

    @Test
    @DisplayName("(d) API falla y caché vacío → propaga ExternalApiException (→ HTTP 502)")
    void fetchAllCategories_ApiFailsAndNoCache_ThrowsException() {
        given(cache.get(any(), eq(List.class))).willReturn(null); // siempre null
        given(categoryApiClient.fetchCategories())
                .willThrow(new ExternalApiException("API no disponible"));

        assertThatThrownBy(() -> categoryService.fetchAllCategories())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("no disponible");
    }

    // ─── Lista inmutable ──────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchAllCategories: retorna List.copyOf (inmutable)")
    void fetchAllCategories_ReturnsImmutableList() {
        given(cache.get(any(), eq(List.class))).willReturn(null);
        given(categoryApiClient.fetchCategories()).willReturn(apiCategories);

        List<CategoryResponse> result = categoryService.fetchAllCategories();

        assertThatThrownBy(() -> result.add(new CategoryResponse(99L, "Extra")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ─── fetchCategoryById ────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchCategoryById: ID existente en caché caliente → sin llamada HTTP")
    void fetchCategoryById_CacheHit_ReturnsCorrectCategory() {
        given(cache.get(any(), eq(List.class))).willReturn(apiCategories);

        CategoryResponse result = categoryService.fetchCategoryById(2L);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Electrónica");
        then(categoryApiClient).should(never()).fetchCategories();
    }

    @Test
    @DisplayName("fetchCategoryById: ID inexistente → lanza ResourceNotFoundException")
    void fetchCategoryById_NotFound_ThrowsException() {
        given(cache.get(any(), eq(List.class))).willReturn(apiCategories);

        assertThatThrownBy(() -> categoryService.fetchCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoría")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("fetchCategoryById: ID null en respuesta de API → Objects.equals evita NPE")
    void fetchCategoryById_NullIdInApiResponse_NoNPE() {
        List<CategoryResponse> withNullId = List.of(
                new CategoryResponse(null, "Sin ID"),
                new CategoryResponse(1L, "Ropa")
        );
        given(cache.get(any(), eq(List.class))).willReturn(withNullId);

        CategoryResponse result = categoryService.fetchCategoryById(1L);

        assertThat(result.name()).isEqualTo("Ropa");
    }
}
