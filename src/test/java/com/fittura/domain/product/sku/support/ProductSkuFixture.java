package com.fittura.domain.product.sku.support;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductSkuFixture {

    private ProductSkuFixture() {}

    public static ProductSku sku(Product product, Long price, Integer stock) {
        return ProductSku.create(
            product,
            price,
            stock,
            "White",
            "Wood"
        );
    }

    public static ProductSku skuWithOption(Product product, String color, String material) {
        return ProductSku.create(
            product,
            10000L,
            100,
            color,
            material
        );
    }

    public static ProductSku skuWithNoOption(Product product) {
        return ProductSku.create(
            product,
            10000L,
            100,
            null,
            null
        );
    }

    public static ProductSku skuWithId(Long id, Product product) {
        ProductSku sku = sku(product, 20000L, 50);
        ReflectionTestUtils.setField(sku, "id", id);
        return sku;
    }
}
