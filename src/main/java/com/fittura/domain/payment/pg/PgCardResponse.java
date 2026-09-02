package com.fittura.domain.payment.pg;

public record PgCardResponse(
    String issuerCode,
    String cardNumberMasked,
    int installmentMonths,
    boolean interestFree,
    String approvalNumber
) {
}
