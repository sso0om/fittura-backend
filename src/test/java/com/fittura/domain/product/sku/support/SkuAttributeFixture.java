package com.fittura.domain.product.sku.support;

import com.fittura.domain.product.sku.constant.AttributeKey;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.entity.SkuAttribute;

public class SkuAttributeFixture {

    private SkuAttributeFixture() {}

    public static SkuAttribute skuAttribute(ProductSku sku, AttributeKey key, String value) {
        return SkuAttribute.create(sku, key, value);
    }

    public static SkuAttribute width(ProductSku sku) {
        return skuAttribute(sku, AttributeKey.WIDTH, "100");
    }

    public static SkuAttribute height(ProductSku sku) {
        return skuAttribute(sku, AttributeKey.HEIGHT, "80");
    }

    public static SkuAttribute depth(ProductSku sku) {
        return skuAttribute(sku, AttributeKey.DEPTH, "60");
    }

    public static SkuAttribute sizeLabel(ProductSku sku) {
        return skuAttribute(sku, AttributeKey.SIZE_LABEL, "L");
    }
}
