package com.fittura.domain.product.sku.support;

import com.fittura.domain.product.sku.entity.Material;

public class MaterialFixture {

    private MaterialFixture() {}

    public static Material material(String name) {
        return Material.create(name);
    }
}
