package com.fittura.domain.order.order.support;

import com.fittura.domain.order.order.entity.Order;
import org.springframework.test.util.ReflectionTestUtils;

public class OrderFixture {

    private OrderFixture() {}

    public static Order order(Long memberId) {
        return Order.create(memberId, 0L);
    }

    public static Order order(Long memberId, Long pointUsedAmount) {
        return Order.create(memberId, pointUsedAmount);
    }

    public static Order orderWithId(Long id, Long memberId) {
        Order order = order(memberId);
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
