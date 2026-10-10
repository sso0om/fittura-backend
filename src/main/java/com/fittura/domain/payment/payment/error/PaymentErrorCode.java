package com.fittura.domain.payment.payment.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    // 400
    NOT_VALID_PG(HttpStatus.BAD_REQUEST, "PM400-01", "결제 처리 중 오류가 발생했습니다"),
    NOT_PAYABLE_STATUS(HttpStatus.BAD_REQUEST, "PM400-02", "결제할 수 없는 상태입니다."),
    UNSUPPORTED_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "PM400-03", "지원하지 않는 결제 방식입니다."),
    PG_REJECTED(HttpStatus.BAD_REQUEST, "PM400-04", "결제가 거절되었습니다."),

    // 404
    NOT_FOUND_PAYMENT(HttpStatus.NOT_FOUND, "PM404-01", "결제 정보를 찾을 수 없습니다."),

    // 502
    PG_RESULT_UNKNOWN(HttpStatus.BAD_GATEWAY, "PM502-01", "결제 결과를 확인 중입니다. 같은 결제를 다시 시도하지 마세요.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
