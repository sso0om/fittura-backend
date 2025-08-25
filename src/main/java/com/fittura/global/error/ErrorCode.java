package com.fittura.global.error;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();

    default HttpStatus httpStatus() {
        HttpStatus httpStatus = HttpStatus.resolve(getStatus());

        return Objects.requireNonNullElse(httpStatus, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
