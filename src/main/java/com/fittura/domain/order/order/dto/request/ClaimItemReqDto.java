package com.fittura.domain.order.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ClaimItemReqDto(
    @NotNull
    Long orderItemId,

    @NotNull @Positive
    Integer quantity
) {
}
