package com.fittura.domain.payment.pg;

public record PgCardResponse(
    String issuerCode,
    String cardNumberMasked,
    Integer installmentMonths,
    boolean interestFree,
    String approvalNumber
) {
}
