package com.fittura.domain.product.sku.dto.responseDto;

import com.fittura.domain.product.sku.constant.AttributeKey;

public record SkuAttributeResDto(
    Long id,
    AttributeKey attributeKey,
    String attributeValue
) {
}
