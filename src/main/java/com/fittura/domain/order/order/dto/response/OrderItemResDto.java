package com.fittura.domain.order.order.dto.response;

import com.fittura.domain.order.order.constant.OrderItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 제품 응답 DTO")
public record OrderItemResDto(
    Long id,
    Long skuId,
    String productName,
    String skuIdentifier,
    Long unitPrice,
    Integer quantity,
    Long discountAmount,
    Long itemTotalAmount,
    OrderItemStatus status
) {
}
