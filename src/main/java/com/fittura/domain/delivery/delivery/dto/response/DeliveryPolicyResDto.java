package com.fittura.domain.delivery.delivery.dto.response;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;

import java.util.Arrays;
import java.util.List;

public record DeliveryPolicyResDto(
    DeliveryType deliveryType,
    Long baseFee,
    Long freeShippingThreshold
) {
    public static List<DeliveryPolicyResDto> all() {
        return Arrays.stream(DeliveryType.values())
            .map(type -> new DeliveryPolicyResDto(
                type,
                type.getBaseFee(),
                type.getFreeShippingThreshold()
            ))
            .toList();
    }
}
