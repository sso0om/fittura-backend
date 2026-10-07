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
        Long id, Long originalPrice, Long discountPrice,
        SkuStatus status, String color, String material, Boolean isSoldOut
    ) {
        this(
            id, originalPrice, PriceCalculator.salePrice(originalPrice, discountPrice),
            PriceCalculator.discountRate(originalPrice, PriceCalculator.salePrice(originalPrice, discountPrice)),
            status, color, material, isSoldOut
        );
    }
}
