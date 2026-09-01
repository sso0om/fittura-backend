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

    PgCardResponse card
) {
}
