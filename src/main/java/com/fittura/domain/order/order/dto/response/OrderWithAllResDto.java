package com.fittura.domain.order.order.dto.response;

import com.fittura.domain.order.order.constant.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 응답 DTO")
public record OrderWithAllResDto(
    Long orderId,
    String orderNumber,
    OrderStatus status,
    LocalDateTime orderDate,
    Long totalAmount,
    Long discountAmount,
    Long pointUsedAmount,
    Long deliveryFee,
    Long finalAmount,
    OrderAddressResDto address,
    List<OrderItemResDto> items
) {
    // Projection 전용 생성자
    public OrderWithAllResDto(
        Long id, String orderNumber, OrderStatus status, LocalDateTime orderDate,
        Long totalAmount, Long discountAmount, Long pointUsedAmount,
        Long deliveryFee, Long finalAmount, OrderAddressResDto address
    ) {
        this(id, orderNumber, status, orderDate, totalAmount, discountAmount,
            pointUsedAmount, deliveryFee, finalAmount, address, List.of());
    }
}
