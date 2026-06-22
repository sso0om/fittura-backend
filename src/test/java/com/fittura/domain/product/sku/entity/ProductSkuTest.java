package com.fittura.domain.product.sku.entity;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSkuTest {

    // ========== sku 생성 ==========

    @Test
    @DisplayName("SKU 생성 성공")
    void createSku() {
        // when
        Product product = ProductFixture.complete("완제품");
        ProductSku productSku = ProductSkuFixture.sku(product, 20000L, 20);

        assertThat(productSku.getProduct()).isEqualTo(product);
        assertThat(productSku.getPrice()).isEqualTo(20000L);
        assertThat(productSku.getStatus()).isEqualTo(SkuStatus.ACTIVE);
    }


    // ========== reserveQuantity ==========

    @Test
    @DisplayName("재고 예약 성공 - reservedQuantity 증가")
    void reserveQuantitySuccess() {
        // given
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 10000L, 10);

        // when
        sku.reserveQuantity(3);

        // then
        assertThat(sku.getReservedQuantity()).isEqualTo(3);
    }


    // ========== isStockValid ==========

    @Test
    @DisplayName("재고 검증 성공")
    void isStockValidTrue() {
        // given
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 10000L, 10);

        // when & then
        assertThat(sku.isStockValid(5)).isTrue();
        assertThat(sku.isStockValid(10)).isTrue();
    }

    @Test
    @DisplayName("재고 검증 실패 - 예약 재고 포함 시 부족")
    void isStockValidFalse_withReserved() {
        // given
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 10000L, 10);
        sku.reserveQuantity(7);

        // when & then
        assertThat(sku.isStockValid(5)).isFalse();
        assertThat(sku.isStockValid(3)).isTrue();
    }


    // ========== getSkuIdentifier ==========

    @Test
    @DisplayName("SKU 식별자 반환 - color, material 모두 있음")
    void getSkuIdentifier_bothOptions() {
        // given
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithOption(product, "White", "Wood");

        // when & then
        assertThat(sku.getSkuIdentifier()).isEqualTo("White / Wood");
    }
}