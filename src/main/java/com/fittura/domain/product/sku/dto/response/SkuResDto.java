package com.fittura.domain.product.sku.dto.response;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.util.PriceCalculator;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SKU 응답 DTO")
public record SkuResDto(
    Long id,
    Long originalPrice,
    Long salePrice,
    Long discountRate,
    SkuStatus status,
    String color,
    String material,
    Boolean isSoldOut
) {
    public SkuResDto(
        Long id, Long price, Long salePrice,
        SkuStatus status, String color, String material, Boolean isSoldOut
    ) {
        this(
            id, price, PriceCalculator.effectivePrice(price, salePrice), PriceCalculator.discountRate(price, salePrice),
            status, color, material, isSoldOut
        );
    }
}
