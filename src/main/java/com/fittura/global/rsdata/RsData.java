package com.fittura.global.rsdata;

import com.fittura.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public record RsData<T>(
        int status,
        String code,
        String message,
        T data
) {
    private static final String DEFAULT_SUCCESS_CODE = "S200-01";
    private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";

    public static RsData<Void> success() {
        return new RsData<>(HttpStatus.OK.value(), DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MESSAGE, null);
    }

    public static <T> RsData<T> success(T data) {
        return new RsData<>(HttpStatus.OK.value(), DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <T> RsData<T> success(String message, T data) {
        return new RsData<>(HttpStatus.OK.value(), DEFAULT_SUCCESS_CODE, message, data);
    }

    public static RsData<Void> error(ErrorCode errorCode) {
        return new RsData<>(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> RsData<T> error(ErrorCode errorCode, T data) {
        return new RsData<>(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage(), data);
    }
}
