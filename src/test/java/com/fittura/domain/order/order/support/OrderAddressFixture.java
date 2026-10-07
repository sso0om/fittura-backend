package com.fittura.domain.order.order.support;

import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.entity.OrderAddress;

public class OrderAddressFixture {

    private OrderAddressFixture() {
    }

    public static OrderAddress address(Order order) {
        return OrderAddress.create(
            order,
            "홍길동",
            "01012341234",
            "12345",
            "서울특별시 중구 서소문로 127",
            "시청역",
            "서울특별시",
            "중구",
            null
        );
    }
}