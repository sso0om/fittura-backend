package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.product.product.constant.AttributeKey;
import com.fittura.domain.product.product.entity.ProductAttribute;

public record ProductAttributeResDto(
    Long id,
    AttributeKey attributeKey,
    String attributeValue
) {
    public static ProductAttributeResDto from(ProductAttribute attribute) {
        return new ProductAttributeResDto(
            attribute.getId(),
            attribute.getAttributeKey(),
            attribute.getAttributeValue()
        );
    }
}
