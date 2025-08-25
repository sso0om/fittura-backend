package com.fittura.global.error;

import jakarta.validation.ConstraintViolation;
import org.springframework.validation.FieldError;

public record ValidationError(
        String field,
        String message
) {
    // @Validated 에서 발생한 ConstraintViolation 을 ValidationError 로 변환
    public static ValidationError from(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        if (field.contains(".")) {
            field = field.substring(field.lastIndexOf(".") + 1);
        }
        return new ValidationError(field, violation.getMessage());
    }

    // @Valid 에서 발생한 FieldError 를 ValidationError 로 변환
    public static ValidationError from(FieldError fieldError) {
        return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
