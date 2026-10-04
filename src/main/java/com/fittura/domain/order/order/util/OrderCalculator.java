package com.fittura.domain.order.order.util;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.delivery.delivery.util.DeliveryFeeCalculator;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class OrderCalculator {

    private OrderCalculator() {}

    public static OrderCalculation calculate(List<OrderItemCalculation> items, long pointUsedAmount) {
        long totalAmount = items.stream()
            .mapToLong(OrderItemCalculation::amount)
            .sum();

        long discountAmount = items.stream()
            .mapToLong(OrderItemCalculation::discountAmount)
            .sum();

        Map<DeliveryType, DeliveryGroupCalculation> groups  = calcGroups(items);
        long deliveryFee = groups.values().stream()
            .mapToLong(DeliveryGroupCalculation::deliveryFee)
            .sum();

        long finalAmount = totalAmount - discountAmount - pointUsedAmount + deliveryFee;

        return new OrderCalculation(
            totalAmount, discountAmount, pointUsedAmount, deliveryFee, finalAmount, groups
        );
    }

    private static Map<DeliveryType, DeliveryGroupCalculation> calcGroups(List<OrderItemCalculation> items) {
        Map<DeliveryType, List<OrderItemCalculation>> itemsByType = items.stream()
            .collect(groupingBy(OrderItemCalculation::deliveryType));

        Map<DeliveryType, DeliveryGroupCalculation> groups = new EnumMap<>(DeliveryType.class);
        itemsByType.forEach(
            (type, group) -> groups.put(type, calcGroup(type, group))
        );
        return groups;
    }

    private static DeliveryGroupCalculation calcGroup(DeliveryType type, List<OrderItemCalculation> items) {
        long amount = items.stream()
            .mapToLong(OrderItemCalculation::itemTotalAmount)
            .sum();

        int quantity = items.stream()
            .mapToInt(OrderItemCalculation::quantity)
            .sum();

        return new DeliveryGroupCalculation(type, amount, DeliveryFeeCalculator.calculate(type, amount, quantity));
    }
}
