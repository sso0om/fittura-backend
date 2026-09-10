package com.fittura.domain.order.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "주문 취소 상품 요청 DTO")
public record ClaimItemReqDto(
    @NotNull
    Long orderItemId,

    @NotNull @Positive
    Integer quantity
) {
}
