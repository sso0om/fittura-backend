package com.fittura.domain.product.product.support;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.entity.Dimension;
import com.fittura.domain.product.product.entity.Product;

public class ProductFixture {

    private static final Dimension DEFAULT_DIMENSION = Dimension.of(40.5, 150.0, 100.0, 50.0);
    private static final Category DEFAULT_CATEGORY = CategoryFixture.rootActive();

    private ProductFixture() {}

    public static Product product(Category category, String name, ProductType productType, Long basePrice) {
        return Product.create(category, name, "상품 설명", productType, basePrice, DEFAULT_DIMENSION);
    }

    public static Product complete(Category category, String name, Long basePrice) {
        return product(category, name, ProductType.COMPLETE, basePrice);
    }

    public static Product component(Category category, String name, Long basePrice) {
        return product(category, name, ProductType.COMPONENT, basePrice);
    }

    public static Product complete(String name, Long basePrice) {
        return complete(DEFAULT_CATEGORY, name, basePrice);
    }

    public static Product component(String name, Long basePrice) {
        return component(DEFAULT_CATEGORY, name, basePrice);
    }
}
