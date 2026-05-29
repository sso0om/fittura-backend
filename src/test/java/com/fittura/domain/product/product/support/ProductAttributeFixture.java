package com.fittura.domain.product.product.support;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.entity.ProductAttribute;
import com.fittura.domain.product.sku.constant.AttributeKey;

public class ProductAttributeFixture {

    private ProductAttributeFixture() {}

    public static ProductAttribute productAttribute(Product product, AttributeKey key, String value) {
        return ProductAttribute.create(product, key, value);
    }
}
