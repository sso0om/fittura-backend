package com.fittura.domain.order.order.dto.response;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.order.order.util.DeliveryGroupCalculation;

import java.util.List;

public record DeliveryGroupResDto(
    DeliveryType deliveryType,
    Long groupAmount,
    Long deliveryFee,
    List<OrderPreviewItemResDto> items
) {
    public static DeliveryGroupResDto of(DeliveryGroupCalculation group, List<OrderPreviewItemResDto> items) {
        return new DeliveryGroupResDto(group.deliveryType(), group.amount(), group.deliveryFee(), items);
    }
}
