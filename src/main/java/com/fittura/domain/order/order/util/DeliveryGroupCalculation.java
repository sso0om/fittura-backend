package com.fittura.domain.order.order.util;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;

public record DeliveryGroupCalculation(
    DeliveryType deliveryType,
    long amount,
    long deliveryFee
) {
}
