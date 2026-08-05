package com.commerce.api.service;

import com.commerce.api.client.CategoryApiClient;
import com.commerce.api.dto.response.CategoryResponse;
import com.commerce.api.exception.ExternalApiException;
import com.commerce.api.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryApiClient categoryApiClient;
    private final CacheManager cacheManager;

    public CategoryServiceImpl(CategoryApiClient categoryApiClient, CacheManager cacheManager) {
        this.categoryApiClient = categoryApiClient;
        this.cacheManager = cacheManager;
    }

    @Override
    public List<CategoryResponse> fetchAllCategories() {
        Cache cache = cacheManager.getCache("categories");

        // 1. Read-through: consultar caché antes de ir a la red
        if (cache != null) {
            @SuppressWarnings("unchecked")
            List<CategoryResponse> cached = cache.get(SimpleKey.EMPTY, List.class);
            if (cached != null && !cached.isEmpty()) {
                log.debug("Categorías servidas desde caché ({} entradas).", cached.size());
                return List.copyOf(cached);
            }
        }

        // 2. Cache miss -> llamar a la API externa
        try {
            List<CategoryResponse> fresh = categoryApiClient.fetchCategories();

            // No cachear lista vacía para no bloquear el fallback en el siguiente intento
            if (cache != null && !fresh.isEmpty()) {
                cache.put(SimpleKey.EMPTY, fresh);
            }
            return List.copyOf(fresh);

        } catch (ExternalApiException e) {
            // 3. API falló -> intentar servir datos stale del caché
            log.warn("API externa de categorías no disponible. Intentando fallback desde caché.");
            if (cache != null) {
                @SuppressWarnings("unchecked")
                List<CategoryResponse> stale = cache.get(SimpleKey.EMPTY, List.class);
                if (stale != null && !stale.isEmpty()) {
                    log.info("Sirviendo {} categorías desde caché (stale-while-revalidate).", stale.size());
                    return List.copyOf(stale);
                }
            }
            throw e;
        }
    }

    @Override
    public CategoryResponse fetchCategoryById(Long id) {
        return fetchAllCategories().stream()
                .filter(c -> Objects.equals(c.id(), id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
    }
}
