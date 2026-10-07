package com.fittura.domain.order.order.dto.response;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.order.order.util.OrderCalculation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.EnumMap;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Schema(description = "주문 전 조회 응답 DTO")
public record OrderPreviewResDto(
    List<DeliveryGroupResDto> deliveryGroups,

    @Schema(description = "Σ(정가 × 수량), 화면 표시용")
    Long totalOriginalAmount,

    @Schema(description = "Σ(판매가 × 수량)")
    Long totalAmount,

    @Schema(description = "쿠폰·프로모션 할인")
    Long discountAmount,

    @Schema(description = "배송 타입별 배송비 합계")
    Long deliveryFee,

    Long finalAmount
) {
    public static OrderPreviewResDto of(List<OrderPreviewItemResDto> items, OrderCalculation calculation) {
        EnumMap<DeliveryType, List<OrderPreviewItemResDto>> itemsByType = items.stream()
            .collect(groupingBy(
                OrderPreviewItemResDto::deliveryType,
                () -> new EnumMap<>(DeliveryType.class), // 응답 묶음 순서 고정
                toList()
            ));

        List<DeliveryGroupResDto> deliveryGroups = itemsByType.entrySet().stream()
            .map(entry ->
                DeliveryGroupResDto.of(
                    calculation.groups().get(entry.getKey()),
                    entry.getValue()
                )
            )
            .toList();

        long totalOriginalAmount  = items.stream()
            .mapToLong(item -> item.originalPrice() * item.quantity())
            .sum();

        return new OrderPreviewResDto(
            deliveryGroups,
            totalOriginalAmount,
            calculation.totalAmount(),
            calculation.discountAmount(),
            calculation.deliveryFee(),
            calculation.finalAmount()
        );
    }
}
