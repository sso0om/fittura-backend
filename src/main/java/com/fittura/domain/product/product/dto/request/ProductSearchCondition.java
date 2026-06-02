package com.fittura.domain.product.product.dto.request;

import com.fittura.domain.product.product.constant.ProductStatus;

import java.util.List;

public record ProductSearchCondition(
    String keyword,
    Long categoryId,
    List<ProductStatus> includedStatuses
) {
}
