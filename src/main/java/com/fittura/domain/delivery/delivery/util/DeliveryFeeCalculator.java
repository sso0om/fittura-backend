package com.fittura.domain.delivery.delivery.util;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;

public class DeliveryFeeCalculator {

    private DeliveryFeeCalculator() {}

    public static long calculate(DeliveryType type, long orderItemAmount, int quantity) {
        return switch (type) {
            case PARCEL -> orderItemAmount >= type.getFreeShippingThreshold()
                ? 0
                : type.getBaseFee();
            case INSTALLATION -> type.getBaseFee() * quantity;
        };
    }
}
