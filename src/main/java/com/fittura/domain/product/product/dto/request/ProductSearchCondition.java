package com.fittura.domain.product.product.dto.request;

import com.fittura.domain.product.product.constant.ProductStatus;

import java.util.List;

public record ProductSearchCondition(
    boolean inStockOnly,
    List<ProductStatus> includedStatuses,
    List<Long> categoryIds,
    String keyword,
    List<Long> colors,
    List<Long> materials
) {
}
