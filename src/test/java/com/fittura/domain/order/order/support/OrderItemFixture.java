package com.fittura.domain.order.order.support;

import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.entity.OrderItem;
import com.fittura.domain.product.sku.entity.ProductSku;

public class OrderItemFixture {

    private OrderItemFixture() {}

    public static OrderItem orderItem(Order order, ProductSku sku, Integer quantity) {
        return OrderItem.create(order, sku, quantity);
    }
}
