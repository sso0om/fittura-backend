package com.fittura.domain.product.sku.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "SKU 생성 요청 DTO")
public record SkuCreateReqDto(

    @Schema(example = "100000")
    @NotNull
    @PositiveOrZero
    Long price,

    @Schema(example = "50")
    @NotNull
    @PositiveOrZero
    Integer stockQuantity,

    @Schema(example = "화이트")
    @Size(max = 50)
    String color,

    @Schema(example = "원목")
    @Size(max = 50)
    String material,

    @Schema(example = "5.5")
    @NotNull
    @PositiveOrZero
    Double weight,

    @Valid
    List<SkuAttributeCreateReqDto> attributes

) {
    public SkuCreateReqDto {
        attributes = attributes != null ? attributes : List.of();
    }
}