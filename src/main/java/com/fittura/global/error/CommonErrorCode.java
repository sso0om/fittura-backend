package com.fittura.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "C4-01", "요청하신 정보를 찾을 수 없습니다."),

    // 400
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "C4-02", "잘못된 요청입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST.value(), "C4-03", "유효성 검사에 실패했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST.value(), "C4-04", "유효하지 않은 입력 값입니다."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "C5-01", "예상치 못한 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
