package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.product.sku.entity.Color;

public record ColorResDto(
    Long id,
    String name
) {
    public static ColorResDto from(Color color) {
        return new ColorResDto(
            color.getId(),
            color.getName()
        );
    }
}
