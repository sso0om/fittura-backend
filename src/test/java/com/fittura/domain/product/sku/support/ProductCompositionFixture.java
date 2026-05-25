package com.fittura.domain.product.sku.support;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.sku.entity.ProductComposition;
import com.fittura.domain.product.sku.entity.ProductSku;

public class ProductCompositionFixture {

    private ProductCompositionFixture() {}

    public static ProductComposition composition(
        Product parentProduct,
        ProductSku childSku,
        Integer quantity,
        Integer sortOrder
    ) {
        return ProductComposition.create(parentProduct, childSku, quantity, sortOrder);
    }

    public static ProductComposition composition(
        Product parentProduct,
        ProductSku childSku
    ) {
        return composition(parentProduct, childSku, 1, 0);
    }
}
