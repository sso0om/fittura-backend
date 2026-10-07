package com.fittura.domain.product.sku.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCalculatorTest {

    // ========== salePrice ==========

    @Test
    @DisplayName("판매가 반환 - 할인가가 있으면 할인가, 없으면 정가")
    void salePrice_returnsDiscountPriceOrOriginalPrice() {
        assertThat(PriceCalculator.salePrice(100_000L, 80_000L)).isEqualTo(80_000L);
        assertThat(PriceCalculator.salePrice(100_000L, null)).isEqualTo(100_000L);
    }


    // ========== discountRate ==========

    @Test
    @DisplayName("할인율 계산")
    void discountRate_returnsPercent() {
        assertThat(PriceCalculator.discountRate(100_000L, 80_000L)).isEqualTo(20L);
        assertThat(PriceCalculator.discountRate(10_000L, 6_667L)).isEqualTo(33L);
        assertThat(PriceCalculator.discountRate(100_000L, 100_000L)).isEqualTo(0L);
        assertThat(PriceCalculator.discountRate(0L, 0L)).isEqualTo(0L);
    }
}
