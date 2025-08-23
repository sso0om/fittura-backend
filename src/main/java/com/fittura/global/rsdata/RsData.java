package com.fittura.global.rsdata;

import com.fittura.global.error.ErrorCode;
import com.fittura.global.error.ValidationError;
import org.springframework.http.HttpStatus;

import java.util.List;

public record RsData<T>(
        int status,
        String code,
        String message,
        T data
) {
    public static RsData<Void> success() {
        return new RsData<>(HttpStatus.OK.value(), "S-01", "요청이 성공적으로 처리되었습니다.", null);
    }

    public static <T> RsData<T> success(T data) {
        return new RsData<>(HttpStatus.OK.value(), "S-01", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> RsData<T> success(String message, T data) {
        return new RsData<>(HttpStatus.OK.value(), "S-01", message, data);
    }

    public static RsData<Void> error(ErrorCode errorCode) {
        return new RsData<>(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> RsData<T> error(ErrorCode errorCode, T data) {
        return new RsData<>(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage(), data);
    }

    public static RsData<List<ValidationError>> validationError(ErrorCode errorCode, List<ValidationError> errors) {
        return new RsData<>(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage(), errors);
    }
}
