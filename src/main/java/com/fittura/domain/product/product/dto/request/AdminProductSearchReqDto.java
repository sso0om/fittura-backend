package com.fittura.domain.product.product.dto.request;

import com.fittura.domain.product.product.constant.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "관리자용 상품 조회 검색 조건 DTO")
public record AdminProductSearchReqDto(
    List<ProductStatus> statuses,
    Long categoryId,
    String keyword,
    List<Long> colors,
    List<Long> materials
) {
}
