package com.fittura.domain.product.sku.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCalculatorTest {

    // ========== effectivePrice ==========

    @Test
    @DisplayName("적용가 반환")
    void effectivePrice_withSalePrice() {
        assertThat(PriceCalculator.effectivePrice(100_000L, 80_000L)).isEqualTo(80_000L);
        assertThat(PriceCalculator.effectivePrice(100_000L, null)).isEqualTo(100_000L);
    }


    // ========== discountRate ==========

    @Test
    @DisplayName("할인율 계산")
    void discountRate_returnsPercent() {
        assertThat(PriceCalculator.discountRate(100_000L, 80_000L)).isEqualTo(20L);
        assertThat(PriceCalculator.discountRate(10_000L, 6_667L)).isEqualTo(33L);
        assertThat(PriceCalculator.discountRate(100_000L, null)).isEqualTo(0L);
        assertThat(PriceCalculator.discountRate(0L, null)).isEqualTo(0L);
    }
}
