package com.fittura.domain.product.product.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeliveryType {
    PARCEL(3000L),
    INSTALLATION(50_000L);

    private final long baseFee;
}
