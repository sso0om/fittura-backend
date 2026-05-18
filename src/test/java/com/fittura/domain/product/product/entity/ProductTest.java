package com.fittura.domain.product.product.entity;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.support.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    // ========== 제품 생성 ==========

    @Test
    @DisplayName("제품 생성 성공")
    void createProduct() {
        // when
        Product product = ProductFixture.complete("완제품", 10000L);

        // then
        assertThat(product.getName()).isEqualTo("완제품");
        assertThat(product.getProductType()).isEqualTo(ProductType.COMPLETE);
        assertThat(product.getBasePrice()).isEqualTo(10000L);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DISABLED);
    }
}