package com.fittura.domain.product.sku.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CompositionCreateReqDto(

    @Schema(example = "10")
    @NotNull
    Long childSkuId,

    @Schema(example = "1")
    @NotNull
    @Min(1)
    Integer quantity,

    @Schema(example = "0")
    @NotNull
    @PositiveOrZero
    Integer sortOrder

) {}