package com.fittura.domain.order.order.dto.response;

import com.fittura.domain.delivery.delivery.constant.DeliveryStatus;
import com.fittura.domain.product.product.constant.DeliveryType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송 정보 DTO")
public record DeliveryResDto(
    Long deliveryId,
    Long orderId,
    DeliveryType deliveryType,
    DeliveryStatus status,
    @Schema(description = "대표 상품명") String repProductName,
    @Schema(description = "상품 종류 수") int itemCnt
) {
}
