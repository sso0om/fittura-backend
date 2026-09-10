package com.fittura.domain.product.product.dto.request;

import com.fittura.domain.product.product.constant.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "상품 조회 검색 조건 DTO")
public record ProductSearchReqDto(
    List<ProductStatus> statuses,
    Long categoryId,
    String keyword,
    List<String> colors,
    List<String> materials
) {
}
