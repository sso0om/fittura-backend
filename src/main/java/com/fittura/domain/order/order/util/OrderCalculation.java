package com.fittura.domain.order.order.util;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;

import java.util.Map;

public record OrderCalculation (
    long totalAmount,
    long discountAmount,
    long pointUsedAmount,
    long deliveryFee,
    long finalAmount,
    Map<DeliveryType, DeliveryGroupCalculation> groups
) {
}
