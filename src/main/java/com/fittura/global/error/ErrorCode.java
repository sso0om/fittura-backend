package com.fittura.global.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();

    default HttpStatus httpStatus() {
        try {
            return HttpStatus.valueOf(getStatus());
        } catch (IllegalArgumentException e) {
            return  HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
