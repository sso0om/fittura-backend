package com.fittura.domain.order.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "장바구니 수정 요청 DTO")
public record CartItemUpdateReqDto(
    @Schema(example = "1")
    @Positive
    Integer quantity
) {
}
