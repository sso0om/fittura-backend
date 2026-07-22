package com.fittura.domain.order.order.dto.response;

import com.fittura.domain.order.order.constant.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 정보 및 배송 상태 DTO")
public record OrderWithDeliveryResDto(
    Long orderId,
    String orderNumber,
    OrderStatus status,
    LocalDateTime orderDate,
    Long finalAmount,
    List<DeliveryResDto> deliveries
) {
    // Projection 전용 생서자
    public OrderWithDeliveryResDto(
        Long orderId, String orderNumber, OrderStatus status,
        LocalDateTime orderDate, Long finalAmount
    ) {
        this(orderId, orderNumber, status, orderDate, finalAmount, List.of());
    }
}
