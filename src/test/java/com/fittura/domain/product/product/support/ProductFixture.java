package com.fittura.domain.product.product.support;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.entity.Product;

public class ProductFixture {

    private ProductFixture() {}

    public static Product product(String name, ProductType productType, Long basePrice) {
        Category category = CategoryFixture.rootActive();

        return Product.create(category, name, "상품 설명", productType, basePrice);
    }

    public static Product complete(String name, Long basePrice) {
        return product(name, ProductType.COMPLETE, basePrice);
    }

    public static Product component(String name, Long basePrice) {
        return product(name, ProductType.COMPONENT, basePrice);
    }
}
