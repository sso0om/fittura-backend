package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.product.sku.entity.Material;

public record MaterialResDto(
    Long id,
    String name
) {
    public static MaterialResDto from(Material material) {
        return new MaterialResDto(
            material.getId(),
            material.getName()
        );
    }
}
