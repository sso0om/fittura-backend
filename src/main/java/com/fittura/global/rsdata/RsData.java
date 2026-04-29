package com.fittura.global.rsdata;

import com.fittura.global.error.ErrorCode;

public record RsData<T>(
        String code,
        String message,
        T data
) {
    private static final String DEFAULT_SUCCESS_CODE = "S200-01";
    private static final String DEFAULT_CREATE_SUCCESS_CODE = "S201-01";

    public static <T> RsData<T> success(T data) {
        return new RsData<>(DEFAULT_SUCCESS_CODE, null, data);
    }

    public static <T> RsData<T> success(String message, T data) {
        return new RsData<>(DEFAULT_SUCCESS_CODE, message, data);
    }

    public static <T> RsData<T> createSuccess(String message, T data) {
        return new RsData<>(DEFAULT_CREATE_SUCCESS_CODE, message, data);
    }

    public static RsData<Void> error(ErrorCode errorCode) {
        return new RsData<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> RsData<T> error(ErrorCode errorCode, T data) {
        return new RsData<>(errorCode.getCode(), errorCode.getMessage(), data);
    }
}
