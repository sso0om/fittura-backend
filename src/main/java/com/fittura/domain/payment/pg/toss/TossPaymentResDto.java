package com.fittura.domain.payment.pg.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResDto(
    String paymentKey,
    String orderId,
    String status,
    Long totalAmount,
    OffsetDateTime approvedAt,
    TossCardResDto card
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TossCardResDto(
        String issuerCode,
        String number,
        Integer installmentPlanMonths,
        Boolean isInterestFree,
        String approveNo
    ) {
    }
}
