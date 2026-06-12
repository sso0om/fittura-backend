package com.fittura.domain.product.sku.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "구성품 생성 요청 DTO")
public record CompositionCreateReqDto(

    @Schema(example = "10")
    @NotNull @Positive
    Long childSkuId,

    @Schema(example = "1")
    @NotNull @Positive
    Integer quantity,

    @Schema(example = "0")
    @NotNull @PositiveOrZero
    Integer sortOrder
) {}