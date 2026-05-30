package com.fittura.domain.product.sku.dto.responseDto;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.entity.ProductSku;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SKU 응답 DTO")
public record SkuResDto(
    Long id,
    Long price,
    SkuStatus status,
    String color,
    String material
) {
    public static SkuResDto from(ProductSku sku) {
        return new SkuResDto(
            sku.getId(),
            sku.getPrice(),
            sku.getStatus(),
            sku.getColor(),
            sku.getMaterial()
        );
    }
}
