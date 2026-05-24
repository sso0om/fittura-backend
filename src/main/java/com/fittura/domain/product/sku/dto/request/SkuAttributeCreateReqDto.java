package com.fittura.domain.product.sku.dto.request;

import com.fittura.domain.product.sku.constant.AttributeKey;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkuAttributeCreateReqDto(

    @Schema(example = "SIZE")
    @NotNull
    AttributeKey attributeKey,

    @Schema(example = "L")
    @NotBlank
    @Size(max = 255)
    String attributeValue

) {}