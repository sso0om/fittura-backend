package com.fittura.domain.product.sku.support;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.sku.entity.Color;
import com.fittura.domain.product.sku.entity.Material;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductSkuFixture {

    private ProductSkuFixture() {}

    public static ProductSku sku(Product product, Long price, Integer stock) {
        return ProductSku.create(
            product,
            price,
            stock,
            null,
            null
        );
    }

    public static ProductSku sku(Product product, Long price, Integer stock, Color color, Material material) {
        return ProductSku.create(
            product,
            price,
            stock,
            color,
            material
        );
    }

    public static ProductSku skuWithOption(Product product, String color, String material) {
        return ProductSku.create(
            product,
            10000L,
            100,
            color != null ? Color.create(color) : null,
            material != null ? Material.create(material) : null
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
