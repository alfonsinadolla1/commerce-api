package com.commerce.api.dto.error;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int           status,
        String        error,
        String        message,
        String        path
) {}
