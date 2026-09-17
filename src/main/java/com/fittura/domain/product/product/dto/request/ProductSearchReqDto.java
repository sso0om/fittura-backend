package com.fittura.domain.product.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "상품 조회 검색 조건 DTO")
public record ProductSearchReqDto(
    Boolean inStockOnly,
    Long categoryId,
    String keyword,
    List<Long> colors,
    List<Long> materials
) {
    public ProductSearchReqDto {
        if (inStockOnly == null) inStockOnly = false;
    }
}
