package com.fittura.domain.order.order.support;

import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.entity.OrderItem;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.test.util.ReflectionTestUtils;

public class OrderFixture {

    private OrderFixture() {
    }

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

    // 아이템 추가 후 금액 계산까지 끝낸 주문
    public static Order orderWithItem(Long memberId, Long pointUsedAmount, ProductSku sku, Integer quantity) {
        Order order = order(memberId, pointUsedAmount);
        OrderItem.create(order, sku, quantity);
        order.calcAmount();
        return order;
    }

    // SKU마다 수량 1개씩 담고 금액 계산까지 끝낸 주문
    public static Order orderWithItems(Long memberId, Long pointUsedAmount, ProductSku... skus) {
        Order order = order(memberId, pointUsedAmount);
        for (ProductSku sku : skus) {
            OrderItem.create(order, sku, 1);
        }
        order.calcAmount();
        return order;
    }
}
