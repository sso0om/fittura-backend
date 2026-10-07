package com.fittura.domain.product.sku.dto.response;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.util.PriceCalculator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SKU 응답 DTO (관리자용)")
public record SkuWithStockResDto(
    Long id,
    Long originalPrice,
    Long salePrice,
    Long discountRate,
    Integer stockQuantity,
    Integer reservedQuantity,
    SkuStatus status,
    String color,
    String material,
    Boolean isSoldOut
) {
    public SkuWithStockResDto(
        Long id, Long originalPrice, Long discountPrice,
        Integer stockQuantity, Integer reservedQuantity,
        SkuStatus status, String color, String material, Boolean isSoldOut
    ) {
        this(
            id, originalPrice, PriceCalculator.salePrice(originalPrice, discountPrice),
            PriceCalculator.discountRate(originalPrice, PriceCalculator.salePrice(originalPrice, discountPrice)),
            stockQuantity, reservedQuantity,
            status, color, material, isSoldOut
        );
    }
}
