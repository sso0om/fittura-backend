package com.fittura.domain.delivery.delivery.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryType {
    PARCEL(4_000L, 40_000L),
    INSTALLATION(30_000L, null);

    private final Long baseFee;
    private final Long freeShippingThreshold;

    public Long calcItemFee(Integer quantity) {
        return this == INSTALLATION ? baseFee * quantity : 0L;
    }
}
