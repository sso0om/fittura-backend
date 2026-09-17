package com.fittura.domain.product.sku.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "SKU 생성 요청 DTO")
public record SkuCreateReqDto(

    @Schema(example = "100000")
    @NotNull @PositiveOrZero
    Long price,

    @Schema(example = "50")
    @NotNull @PositiveOrZero
    Integer stockQuantity,

    @Schema(example = "1")
    @Positive
    Long colorId,

    @Schema(example = "1")
    @Positive
    Long materialId
) {
}