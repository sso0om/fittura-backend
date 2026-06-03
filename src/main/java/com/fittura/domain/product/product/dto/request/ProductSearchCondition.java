package com.fittura.domain.product.product.dto.request;

import com.fittura.domain.product.product.constant.ProductStatus;

import java.util.List;

public record ProductSearchCondition(
    List<ProductStatus> includedStatuses,
    Long categoryId,
    String keyword,
    List<String> colors,
    List<String> materials
) {
}
