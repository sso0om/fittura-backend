package com.fittura.domain.product.product.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;

import java.time.LocalDateTime;

public record ProductResDto(
    Long id,
    String name,
    Long basePrice,
    ProductStatus status,
    ProductType productType,
    @JsonIgnore LocalDateTime createdDate
) {
}
