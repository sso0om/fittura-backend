package com.fittura.global.error;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public interface ErrorCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();

    default HttpStatus httpStatus() {
        return Objects.requireNonNullElse(getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
