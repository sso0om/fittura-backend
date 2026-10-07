package com.fittura.domain.order.order.util;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCalculatorTest {

    // ========== 금액 ==========

    @Test
    @DisplayName("calculate 성공 - 최종 금액 = 합계 - 할인 - 포인트 + 배송비")
    void calculateSuccess_finalAmount() {
        // given
        List<OrderItemCalculation> items = List.of(
            parcel(10000L, 2, 1000L),   // 합계 20000, 할인 1000
            parcel(5000L, 1, 0L)        // 합계 5000
        );

        // when
        OrderCalculation result = OrderCalculator.calculate(items, 500L);

        // then
        // 할인 후 금액 24000 → 무료배송 기준 미만
        long deliveryFee = DeliveryType.PARCEL.getBaseFee();
        assertThat(result.totalAmount()).isEqualTo(25000L);
        assertThat(result.discountAmount()).isEqualTo(1000L);
        assertThat(result.pointUsedAmount()).isEqualTo(500L);
        assertThat(result.deliveryFee()).isEqualTo(deliveryFee);
        assertThat(result.finalAmount()).isEqualTo(25000L - 1000L - 500L + deliveryFee);
    }


    // ========== 배송비 ==========

    @Test
    @DisplayName("calculate 성공 - 무료배송 기준은 할인 후 금액으로 판단")
    void calculateSuccess_parcelThresholdAfterDiscount() {
        // given
        List<OrderItemCalculation> items = List.of(
            parcel(40000L, 1, 5000L)   // 할인 전 40000, 할인 후 35000
        );

        // when
        OrderCalculation result = OrderCalculator.calculate(items, 0L);

        // then
        assertThat(result.deliveryFee()).isEqualTo(DeliveryType.PARCEL.getBaseFee());
    }

    @Test
    @DisplayName("calculate 성공 - 일반배송과 기사배송이 섞이면 타입별로 묶어 계산하고 합산")
    void calculateSuccess_mixedTypes() {
        // given
        List<OrderItemCalculation> items = List.of(
            new OrderItemCalculation(DeliveryType.INSTALLATION, 200000L, 2, 0L),
            parcel(10000L, 1, 0L)
        );

        // when
        OrderCalculation result = OrderCalculator.calculate(items, 0L);

        // then
        long parcelFee = DeliveryType.PARCEL.getBaseFee();
        long installationFee = DeliveryType.INSTALLATION.getBaseFee() * 2;

        assertThat(result.groups().get(DeliveryType.PARCEL))
            .isEqualTo(new DeliveryGroupCalculation(DeliveryType.PARCEL, 10000L, parcelFee));
        assertThat(result.groups().get(DeliveryType.INSTALLATION))
            .isEqualTo(new DeliveryGroupCalculation(DeliveryType.INSTALLATION, 400000L, installationFee));
        assertThat(result.deliveryFee()).isEqualTo(parcelFee + installationFee);
    }

    @Test
    @DisplayName("calculate 성공 - 묶음 순서는 입력 순서와 상관없이 PARCEL → INSTALLATION")
    void calculateSuccess_groupOrder() {
        // given
        List<OrderItemCalculation> items = List.of(
            new OrderItemCalculation(DeliveryType.INSTALLATION, 200000L, 1, 0L),
            parcel(10000L, 1, 0L)
        );

        // when
        OrderCalculation result = OrderCalculator.calculate(items, 0L);

        // then
        assertThat(result.groups().keySet())
            .containsExactly(DeliveryType.PARCEL, DeliveryType.INSTALLATION);
    }

    @Test
    @DisplayName("calculate 성공 - 묶음 합계는 할인 후 금액")
    void calculateSuccess_groupAmountAfterDiscount() {
        // given
        List<OrderItemCalculation> items = List.of(
            parcel(30000L, 1, 5000L),
            parcel(10000L, 1, 0L)
        );

        // when
        OrderCalculation result = OrderCalculator.calculate(items, 0L);

        // then
        assertThat(result.groups().get(DeliveryType.PARCEL).amount()).isEqualTo(35000L);
    }


    // ========== 헬퍼 메서드 ==========

    private OrderItemCalculation parcel(long salePrice, int quantity, long discountAmount) {
        return new OrderItemCalculation(DeliveryType.PARCEL, salePrice, quantity, discountAmount);
    }
}
