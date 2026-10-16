package com.fittura.domain.payment.pg.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossErrorResDto(
    String code,
    String message
) {
}
