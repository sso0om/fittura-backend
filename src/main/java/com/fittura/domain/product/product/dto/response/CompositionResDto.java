package com.fittura.domain.product.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구성품 응답 DTO")
public record CompositionResDto(
    Long childSkuId,
    String childProductName,
    Integer quantity,
    Integer sortOrder
) {}
