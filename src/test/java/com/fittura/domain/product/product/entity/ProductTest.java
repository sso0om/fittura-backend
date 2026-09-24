package com.fittura.domain.product.product.entity;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import com.fittura.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    // ========== 제품 생성 ==========

    @Test
    @DisplayName("제품 생성 성공")
    void createProduct() {
        // when
        Product product = ProductFixture.complete("완제품");

        // then
        assertThat(product.getName()).isEqualTo("완제품");
        assertThat(product.getProductType()).isEqualTo(ProductType.COMPLETE);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DISABLED);
    }


    // ========== basePrice 동기화 ==========

    @Test
    @DisplayName("basePrice 동기화 성공 - 단일 SKU 가격으로 설정")
    void syncBasePrice_singleSku() {
        // given
        Product product = ProductFixture.component("책상");
        ProductSkuFixture.sku(product, 30000L, 10);

        // when
        product.syncBasePrice();

        // then
        assertThat(product.getBasePrice()).isEqualTo(30000L);
    }

    @Test
    @DisplayName("basePrice 동기화 성공 - price, salePrice 중 최솟값인 SKU")
    void syncBasePrice_usesEffectivePriceWithSalePrice() {
        // given
        Product product = ProductFixture.component("책상");
        ProductSkuFixture.sku(product, 40000L, 10);
        ProductSkuFixture.sku(product, 50000L, 30000L, 5);

        // when
        product.syncBasePrice();

        // then
        assertThat(product.getBasePrice()).isEqualTo(50000L);
        assertThat(product.getBaseSalePrice()).isEqualTo(30000L);
    }

    @Test
    @DisplayName("basePrice 동기화 성공 - PAUSED SKU가 더 싸도 ACTIVE SKU 우선")
    void syncBasePrice_activePreferredOverPaused() {
        // given
        Product product = ProductFixture.component("책상");
        ProductSkuFixture.sku(product, 50000L, 10);

        ProductSku pausedSku = ProductSkuFixture.sku(product, 30000L, 5);
        ReflectionTestUtils.setField(pausedSku, "status", SkuStatus.PAUSED);

        // when
        product.syncBasePrice();

        // then
        assertThat(product.getBasePrice()).isEqualTo(50000L);
        assertThat(product.getBaseSalePrice()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("basePrice 동기화 성공 - ACTIVE SKU가 없으면 ARCHIVED 제외 SKU 중 최솟값으로 설정")
    void syncBasePrice_fallbackToNonArchived_whenNoActive() {
        // given
        Product product = ProductFixture.component("책상");
        ProductSku pausedSku = ProductSkuFixture.sku(product, 40000L, 5);
        ReflectionTestUtils.setField(pausedSku, "status", SkuStatus.PAUSED);

        ProductSku discontinuedSku = ProductSkuFixture.sku(product, 50000L, 30000L, 5);
        ReflectionTestUtils.setField(discontinuedSku, "status", SkuStatus.DISCONTINUED);

        ProductSku archivedSku = ProductSkuFixture.sku(product, 10000L, 5);
        ReflectionTestUtils.setField(archivedSku, "status", SkuStatus.ARCHIVED);

        // when
        product.syncBasePrice();

        // then
        assertThat(product.getBasePrice()).isEqualTo(50000L);
        assertThat(product.getBaseSalePrice()).isEqualTo(30000L);
    }

    @Test
    @DisplayName("basePrice 동기화 실패 - SKU 없음")
    void syncBasePrice_fail_noSku() {
        // given
        Product product = ProductFixture.component("책상");

        // when & then
        assertThatThrownBy(product::syncBasePrice)
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_HAVA_SKU);
    }

    @Test
    @DisplayName("basePrice 동기화 실패 - 활성 SKU 없음(전부 ARCHIVED)")
    void syncBasePrice_fail_allSkusArchived() {
        // given
        Product product = ProductFixture.component("책상");
        ProductSku archivedSku = ProductSkuFixture.sku(product, 10000L, 5);
        ReflectionTestUtils.setField(archivedSku, "status", SkuStatus.ARCHIVED);

        // when & then
        assertThatThrownBy(product::syncBasePrice)
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_HAVA_SKU);
    }
}