package com.fittura.domain.product.sku.dto.responseDto;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.entity.ProductSku;

public record SkuResDto(
    Long id,
    Long price,
    Integer stockQuantity,
    Integer reservedQuantity,
    SkuStatus status,
    String color,
    String material,
    Double weight
    //List<SkuAttributeResDto> attributes
) {
    public static SkuResDto from(ProductSku sku) {
        return new SkuResDto(
            sku.getId(),
            sku.getPrice(),
            sku.getStockQuantity(),
            sku.getReservedQuantity(),
            sku.getStatus(),
            sku.getColor(),
            sku.getMaterial(),
            sku.getWeight()
        );
    }
}
