package com.fittura.domain.order.cart.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {
    QUANTITY_MUST_BE_POSITIVE(HttpStatus.BAD_REQUEST, "CA400-01", "수량은 1개 이상 선택해야 합니다."),
    QUANTITY_EXCEEDED(HttpStatus.BAD_REQUEST, "CA400-02", "수량은 999개 이하 선택해야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
