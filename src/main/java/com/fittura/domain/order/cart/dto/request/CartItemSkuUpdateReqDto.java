package com.fittura.domain.order.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "장바구니 SKU 수정 요청 DTO")
public record CartItemSkuUpdateReqDto(
    @Schema(example = "1")
    @NotNull @Positive
    Long skuId,

    @Schema(example = "1")
    @NotNull @Min(1) @Max(999)
    Integer quantity
) {
}
