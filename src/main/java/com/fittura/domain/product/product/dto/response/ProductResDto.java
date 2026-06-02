package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;

public record ProductResDto(
    Long id,
    String name,
    Long basePrice,
    ProductStatus status,
    ProductType productType
) {
}
