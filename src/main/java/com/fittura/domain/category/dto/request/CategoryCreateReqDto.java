package com.fittura.domain.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "카테고리 생성 요청 DTO")
public record CategoryCreateReqDto(
    @NotBlank @Size(max = 255)
    String name,

    Long parentId,

    @PositiveOrZero
    int sortOrder
) {
}
