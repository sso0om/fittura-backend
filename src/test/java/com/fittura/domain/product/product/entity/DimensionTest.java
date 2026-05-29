package com.fittura.domain.product.product.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DimensionTest {

    @Test
    @DisplayName("Dimension 생성 실패 - weight가 0 이하")
    void createFail_invalidWeight() {
        assertThatThrownBy(() -> Dimension.of(0.0, 150.0, 100.0, 50.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Dimension 생성 실패 - width가 0 이하")
    void createFail_invalidWidth() {
        assertThatThrownBy(() -> Dimension.of(40.5, 0.0, 100.0, 50.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Dimension 생성 실패 - height가 0 이하")
    void createFail_invalidHeight() {
        assertThatThrownBy(() -> Dimension.of(40.5, 150.0, 0.0, 50.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Dimension 생성 실패 - depth가 0 이하")
    void createFail_invalidDepth() {
        assertThatThrownBy(() -> Dimension.of(40.5, 150.0, 100.0, 0.0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}