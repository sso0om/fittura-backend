package com.fittura.domain.product.sku.dto.responseDto;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.entity.ProductSku;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SKU 응답 DTO (관리자용)")
public record SkuWithStockResDto(
    Long id,
    Long price,
    Integer stockQuantity,
    Integer reservedQuantity,
    SkuStatus status,
    String color,
    String material
) {
    public static SkuWithStockResDto from(ProductSku sku) {
        return new SkuWithStockResDto(
            sku.getId(),
            sku.getPrice(),
            sku.getStockQuantity(),
            sku.getReservedQuantity(),
            sku.getStatus(),
            sku.getColor(),
            sku.getMaterial()
        );
    }
}
