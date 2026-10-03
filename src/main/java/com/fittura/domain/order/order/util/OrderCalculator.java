package com.fittura.domain.order.order.util;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.delivery.delivery.util.DeliveryFeeCalculator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class OrderCalculator {

    private OrderCalculator() {}

    public static OrderCalculation calculate(List<OrderItemCalculation> items, long pointUsedAmount) {
        long totalAmount = items.stream()
            .mapToLong(OrderItemCalculation::amount)
            .sum();

        long discountAmount = items.stream()
            .mapToLong(OrderItemCalculation::discountAmount)
            .sum();

        Map<DeliveryType, Long> deliveryFeeByType = calcDeliveryFeeByType(items);
        long deliveryFee = deliveryFeeByType.values().stream()
            .mapToLong(Long::longValue)
            .sum();

        long finalAmount = totalAmount - discountAmount - pointUsedAmount + deliveryFee;

        return new OrderCalculation(
            totalAmount, discountAmount, pointUsedAmount, deliveryFee, finalAmount, deliveryFeeByType
        );
    }

    private static Map<DeliveryType, Long> calcDeliveryFeeByType(List<OrderItemCalculation> items) {
        Map<DeliveryType, List<OrderItemCalculation>> itemsByType = items.stream()
            .collect(Collectors.groupingBy(OrderItemCalculation::deliveryType));

        return itemsByType
            .entrySet().stream()
            .collect(toMap(
                Map.Entry::getKey,
                entry -> calcGroupFee(entry.getKey(), entry.getValue())
            ));
    }

    private static long calcGroupFee(DeliveryType type, List<OrderItemCalculation> items) {
        long amount = items.stream()
            .mapToLong(OrderItemCalculation::itemTotalAmount)
            .sum();

        int quantity = items.stream()
            .mapToInt(OrderItemCalculation::quantity)
            .sum();

        return DeliveryFeeCalculator.calculate(type, amount, quantity);
    }
}
