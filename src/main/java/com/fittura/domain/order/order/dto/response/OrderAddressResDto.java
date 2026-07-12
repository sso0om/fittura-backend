package com.fittura.domain.order.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 주소 응답 DTO")
public record OrderAddressResDto(
    String receiverName,
    String phoneNumber,
    String zipCode,
    String address,
    String addressDetail,
    String sido,
    String sigungu,
    String deliveryMemo
) {
}
