package com.fittura.domain.payment.pg.toss;

public record TossConfirmReqDto(
    String paymentKey,
    String orderId,
    Long amount
) {
}
