package com.fittura.domain.product.categry.dto.request;

import com.fittura.domain.product.categry.constant.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CategoryUpdateReqDto(
    @NotBlank @Size(max = 255)
    String name,
    Long parentId,
    @PositiveOrZero
    int sortOrder,
    @NotNull
    CategoryStatus status
) {
}
