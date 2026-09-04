package com.fittura.domain.payment.pg;

public record PgConfirmCommand(
    String paymentKey,
    String paymentNumber,
    Long amount
) {
}
