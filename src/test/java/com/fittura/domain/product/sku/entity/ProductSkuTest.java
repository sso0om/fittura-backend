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
        Product product = ProductFixture.complete("완제품", 20000L);
        ProductSku productSku = ProductSkuFixture.sku(product, 20000L, 20);

        assertThat(productSku.getProduct()).isEqualTo(product);
        assertThat(productSku.getPrice()).isEqualTo(20000L);
        assertThat(productSku.getStatus()).isEqualTo(SkuStatus.ACTIVE);
    }
}