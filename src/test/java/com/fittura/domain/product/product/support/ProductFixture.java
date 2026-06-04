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

    public static Product product(Category category, String name, ProductType productType) {
        return Product.create(category, name, "상품 설명", productType, DEFAULT_DIMENSION);
    }

    public static Product complete(Category category, String name) {
        return product(category, name, ProductType.COMPLETE);
    }

    public static Product component(Category category, String name) {
        return product(category, name, ProductType.COMPONENT);
    }

    public static Product complete(String name) {
        return complete(DEFAULT_CATEGORY, name);
    }

    public static Product component(String name) {
        return component(DEFAULT_CATEGORY, name);
    }
}
