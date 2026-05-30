package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.product.product.constant.AttributeKey;

public record ProductAttributeResDto(
    Long id,
    AttributeKey attributeKey,
    String attributeValue
) {
}
