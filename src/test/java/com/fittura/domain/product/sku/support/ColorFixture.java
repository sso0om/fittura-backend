package com.fittura.domain.product.sku.support;

import com.fittura.domain.product.sku.entity.Color;

public class ColorFixture {

    private ColorFixture() {
    }

    public static Color color(String name) {
        return Color.create(name);
    }
}
