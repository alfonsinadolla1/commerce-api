package com.commerce.api.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
        String name,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 8 enteros y 2 decimales")
        BigDecimal price,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        @Max(value = 1_000_000, message = "El stock no puede superar 1.000.000")
        Integer stock,

        @NotNull(message = "El categoryId es obligatorio")
        @Positive(message = "El categoryId debe ser mayor a cero")
        Long categoryId

) {}
