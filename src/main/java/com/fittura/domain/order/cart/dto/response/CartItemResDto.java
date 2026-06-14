package com.fittura.domain.order.cart.dto.response;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.sku.constant.SkuStatus;

public record CartItemResDto(
    Long cartItemId,
    Long productId,
    String productName,
    Long skuId,
    String color,
    String material,
    Long unitPrice,
    Integer quantity,
    Long itemTotalPrice,
    ProductStatus productStatus,
    SkuStatus skuStatus
) {
}
