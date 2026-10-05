package com.fittura.domain.order.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "바로 주문 SKU 요청 DTO")
public record OrderSkuReqDto(
    @Schema(example = "1")
    @NotNull @Positive
    Long skuId,

    @Schema(example = "1")
    @NotNull @Min(1) @Max(999)
    Integer quantity
) {
}
