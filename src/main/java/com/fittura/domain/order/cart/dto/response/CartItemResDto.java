package com.fittura.domain.order.cart.dto.response;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.util.PriceCalculator;

public record CartItemResDto(
    Long cartItemId,
    /* product */
    Long productId,
    String productName,
    String mainImageUrl,
    ProductStatus productStatus,
    DeliveryType deliveryType,
    Long deliveryFee,
    /* sku */
    Long skuId,
    String color,
    String material,
    Long originalPrice,
    Long salePrice,
    Long discountRate,
    Integer quantity,
    Long itemTotalPrice,
    SkuStatus skuStatus,
    Boolean isSoldOut
) {
    // Projection 전용 생성자
    public CartItemResDto(
        Long cartItemId, Long productId, String productName, String mainImageUrl,
        ProductStatus productStatus, DeliveryType deliveryType,
        Long skuId, String color, String material,
        Long price, Long salePrice, Integer quantity, SkuStatus skuStatus, Boolean isSoldOut
    ) {
        this(
            cartItemId, productId, productName, mainImageUrl, productStatus,
            deliveryType,
            deliveryType.calcItemFee(quantity),
            skuId, color, material,
            price,
            PriceCalculator.effectivePrice(price, salePrice),
            PriceCalculator.discountRate(price, salePrice),
            quantity,
            PriceCalculator.effectivePrice(price, salePrice) * quantity,
            skuStatus,
            isSoldOut
        );
    }
}
