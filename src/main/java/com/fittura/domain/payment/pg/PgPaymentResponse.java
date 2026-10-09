package com.fittura.domain.payment.pg;

import com.fittura.domain.payment.payment.constant.PaymentStatus;

import java.time.LocalDateTime;

public record PgPaymentResponse(
    String paymentKey,
    String paymentNumber,
    PaymentStatus status,
    Long totalAmount,
    LocalDateTime approvedDate,
    String rawResponse,
    String errorCode,
    String errorMessage,

    PgCardResponse card
) {
    public static PgPaymentResponse rejected(String key, String number, String rawResponse, String code, String msg) {
        return new PgPaymentResponse(
            key, number, PaymentStatus.FAILED,
            null, null, rawResponse,
            code, msg, null
        );
    }

    public static PgPaymentResponse unknown(String key, String number, String rawResponse, String code) {
        return new PgPaymentResponse(
            key, number, PaymentStatus.UNKNOWN,
            null, null, rawResponse,
            code, "결제 결과를 확인 중입니다.", null
        );
    }
}
