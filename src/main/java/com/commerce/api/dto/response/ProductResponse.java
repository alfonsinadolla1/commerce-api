package com.commerce.api.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        Long       id,
        String     name,
        BigDecimal price,
        Integer    stock,
        Long       categoryId,
        String     categoryName
) {}
