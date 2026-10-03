package com.fittura.domain.order.order.util;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.product.sku.entity.ProductSku;

public record OrderItemCalculation(
    DeliveryType deliveryType,
    long unitPrice,
    int quantity,
    long discountAmount
) {
    public static OrderItemCalculation of(ProductSku sku, int quantity) {
        return new OrderItemCalculation(
            sku.getProduct().getDeliveryType(),
            sku.getEffectivePrice(),
            quantity,
            0L
        );
    }

    // 할인 전 금액
    public long amount() {
        return unitPrice * quantity;
    }

    // 할인 후 금액 (= OrderItem.itemTotalAmount)
    public long itemTotalAmount() {
        return amount() - discountAmount;
    }
}
