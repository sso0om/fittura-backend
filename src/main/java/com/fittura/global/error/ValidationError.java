package com.fittura.global.error;

public record ValidationError(
        String field,
        String message
) {}
