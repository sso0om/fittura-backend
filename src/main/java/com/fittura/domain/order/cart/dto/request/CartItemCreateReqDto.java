package com.fittura.domain.order.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemCreateReqDto(
    @Schema(example = "1")
    @NotNull @Positive
    Long skuId,

    @Schema(example = "1")
    @Positive
    Integer quantity
) {
}
