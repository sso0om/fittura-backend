package com.fittura.domain.delivery.delivery.support;

import com.fittura.domain.delivery.delivery.entitiy.Delivery;
import com.fittura.domain.product.product.constant.DeliveryType;

public class DeliveryFixture {

    private DeliveryFixture() {}

    public static Delivery parcel(Long orderId) {
        return Delivery.create(orderId, DeliveryType.PARCEL, 3000L);
    }

    public static Delivery installation(Long orderId) {
        return Delivery.create(orderId, DeliveryType.INSTALLATION, 50000L);
    }

    public static Delivery delivery(Long orderId, DeliveryType type, Long fee) {
        return Delivery.create(orderId, type, fee);
    }
}