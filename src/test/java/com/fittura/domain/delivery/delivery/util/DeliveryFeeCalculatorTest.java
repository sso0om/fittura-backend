package com.fittura.domain.delivery.delivery.util;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryFeeCalculatorTest {

    // ========== 일반배송 ==========

    @Test
    @DisplayName("일반배송 - 무료배송 기준 미만이면 기본 배송비")
    void parcel_belowThreshold() {
        long threshold = DeliveryType.PARCEL.getFreeShippingThreshold();

        assertThat(DeliveryFeeCalculator.calculate(DeliveryType.PARCEL, threshold - 1, 3))
            .isEqualTo(DeliveryType.PARCEL.getBaseFee());
    }

    @Test
    @DisplayName("일반배송 - 무료배송 기준 이상이면 0")
    void parcel_atOrAboveThreshold() {
        long threshold = DeliveryType.PARCEL.getFreeShippingThreshold();

        assertThat(DeliveryFeeCalculator.calculate(DeliveryType.PARCEL, threshold, 1)).isZero();
        assertThat(DeliveryFeeCalculator.calculate(DeliveryType.PARCEL, threshold + 1, 1)).isZero();
    }


    // ========== 기사배송 ==========

    @Test
    @DisplayName("기사배송 - 기본 배송비 x 수량, 금액과 무관")
    void installation_perQuantity() {
        long baseFee = DeliveryType.INSTALLATION.getBaseFee();

        assertThat(DeliveryFeeCalculator.calculate(DeliveryType.INSTALLATION, 1_000_000L, 3))
            .isEqualTo(baseFee * 3);
    }
}
