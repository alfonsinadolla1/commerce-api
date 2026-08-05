package com.commerce.api.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super("No se encontró %s con id: %d".formatted(resource, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
