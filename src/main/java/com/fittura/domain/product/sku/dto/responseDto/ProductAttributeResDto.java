package com.fittura.domain.product.sku.dto.responseDto;

import com.fittura.domain.product.product.constant.AttributeKey;

public record ProductAttributeResDto(
    Long id,
    AttributeKey attributeKey,
    String attributeValue
) {
}
