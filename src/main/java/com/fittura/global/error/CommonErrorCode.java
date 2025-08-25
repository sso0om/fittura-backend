package com.fittura.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "C404-01", "요청하신 정보를 찾을 수 없습니다."),

    // 400
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "C400-01", "잘못된 요청입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST.value(), "C400-02", "유효성 검사에 실패했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST.value(), "C400-03", "유효하지 않은 입력 값입니다."),

    // security 401, 403
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "C401-1", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), "C403-1", "접근 권한이 없습니다."),

    // 409, 429
    CONFLICT(HttpStatus.CONFLICT.value(), "C409-01", "리소스 상태 충돌이 발생했습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS.value(), "C429-01", "잠시 후 다시 시도하세요."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "C500-01", "예상치 못한 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
