package com.fittura.domain.order.order.entity;

import com.fittura.domain.order.order.constant.OrderStatus;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.support.OrderFixture;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import com.fittura.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    // ========== 주문 생성 ==========

    @Test
    @DisplayName("주문 생성 성공")
    void createSuccess() {
        // when
        Order order = Order.create(1L, 0L);

        // then
        assertThat(order.getMemberId()).isEqualTo(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualTo(0L);
        assertThat(order.getDiscountAmount()).isEqualTo(0L);
        assertThat(order.getPointUsedAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("주문 생성 실패 - memberId null")
    void createFail_nullMemberId() {
        assertThatThrownBy(() -> Order.create(null, 0L))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("주문 생성 실패 - 포인트 음수")
    void createFail_negativePoint() {
        assertThatThrownBy(() -> Order.create(1L, -1L))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(OrderErrorCode.AMOUNT_MUST_BE_POSITIVE);
    }


    // ========== addItem ==========

    @Test
    @DisplayName("addItem 성공 - totalAmount 누적")
    void addItemSuccess() {
        // given
        Order order = OrderFixture.order(1L);
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 10000L, 100);

        // when
        OrderItem.create(order, sku, 3);

        // then
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualTo(30000L);
    }

    @Test
    @DisplayName("addItem 성공 - 여러 아이템 추가 시 totalAmount 합산")
    void addItemSuccess_multipleItems() {
        // given
        Order order = OrderFixture.order(1L);
        Product product = ProductFixture.component("A Desk");
        ProductSku sku1 = ProductSkuFixture.sku(product, 10000L, 100);
        ProductSku sku2 = ProductSkuFixture.sku(product, 20000L, 100);

        // when
        OrderItem.create(order, sku1, 2);
        OrderItem.create(order, sku2, 1);

        // then
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotalAmount()).isEqualTo(40000L);
    }


    // ========== calcFinalAmount ==========

    @Test
    @DisplayName("calcFinalAmount 성공")
    void calcFinalAmountSuccess_withItems() {
        // given
        Order order = OrderFixture.order(1L, 1000L);
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 10000L, 100);
        OrderItem.create(order, sku, 2); // totalAmount = 20000

        // when
        order.calcFinalAmount();

        // then
        // finalAmount = 20000 - 0 - 1000 + 4000 = 23000
        assertThat(order.getFinalAmount()).isEqualTo(23000L);
    }
}