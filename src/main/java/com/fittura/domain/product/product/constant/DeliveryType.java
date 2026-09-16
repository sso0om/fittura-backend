package com.fittura.domain.product.product.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryType {
    PARCEL(4000L),
    INSTALLATION(50_000L);

    private final Long baseFee;
}
