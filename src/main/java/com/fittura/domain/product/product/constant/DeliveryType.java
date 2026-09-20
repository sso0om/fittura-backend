package com.fittura.domain.product.product.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryType {
    PARCEL(4_000L, 40_000L),
    INSTALLATION(30_000L, null);

    private final Long baseFee;
    private final Long freeShippingThreshold;
}
