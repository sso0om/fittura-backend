package com.fittura.domain.product.product.dto.request;

import com.fittura.domain.product.product.constant.AttributeKey;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AttributeUpdateReqDto(

    @Schema(example = "1")
    @Positive
    Long id,

    @Schema(example = "SIZE_LABEL")
    @NotNull
    AttributeKey attributeKey,

    @Schema(example = "L")
    @NotBlank @Size(max = 255)
    String attributeValue
) {
}
