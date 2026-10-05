package com.fittura.domain.order.order.entity;

import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.order.order.constant.OrderStatus;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.support.OrderFixture;
import com.fittura.domain.product.product.constant.ProductType;
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
    @DisplayName("addItem 성공 - 주문에 아이템 추가")
    void addItemSuccess() {
        // given
        Order order = OrderFixture.order(1L);
        Product product = ProductFixture.component("A Desk");

        // when
        OrderItem.create(order, ProductSkuFixture.sku(product, 10000L, 100), 2);
        OrderItem.create(order, ProductSkuFixture.sku(product, 20000L, 100), 1);

        // then
        assertThat(order.getItems()).hasSize(2);
    }


    // ========== calcAmount ==========

    @Test
    @DisplayName("calcAmount 성공 - 할인 상품은 판매가 기준으로 계산")
    void calcAmountSuccess_saleSku() {
        // given
        Order order = OrderFixture.order(1L);
        ProductSku sku = ProductSkuFixture.sku(product(DeliveryType.PARCEL), 10000L, 8000L, 100);
        OrderItem.create(order, sku, 2);

        // when
        order.calcAmount();

        // then
        assertThat(order.getTotalAmount()).isEqualTo(16000L);
    }


    // ========== 헬퍼 메서드 ==========

    private Product product(DeliveryType deliveryType) {
        return ProductFixture.product(CategoryFixture.rootActive(), "A Desk", ProductType.COMPONENT, deliveryType);
    }
}