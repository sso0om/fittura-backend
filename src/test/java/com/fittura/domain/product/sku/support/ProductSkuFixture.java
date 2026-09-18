package com.fittura.domain.product.sku.support;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.sku.entity.Color;
import com.fittura.domain.product.sku.entity.Material;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductSkuFixture {

    private ProductSkuFixture() {
    }

    public static ProductSku sku(Product product, Long price, Long salePrice, Integer stock) {
        return ProductSku.create(
            product,
            price,
            salePrice,
            stock,
            null,
            null
        );
    }

    public static ProductSku sku(Product product, Long price, Integer stock) {
        return sku(product, price, null, stock);
    }

    public static ProductSku sku(Product product, Long price, Integer stock, Color color, Material material) {
        return ProductSku.create(
            product,
            price,
            null,
            stock,
            color,
            material
        );
    }

    public static ProductSku skuWithNoOption(Product product) {
        return ProductSku.create(
            product,
            10000L,
            null,
            100,
            null,
            null
        );
    }

    public static ProductSku skuWithId(Long id, Product product, Long price) {
        ProductSku sku = sku(product, price, 50);
        ReflectionTestUtils.setField(sku, "id", id);
        return sku;
    }

    public static ProductSku skuWithId(Long id, Product product) {
        return skuWithId(id, product, null);
    }
}
