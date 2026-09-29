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
        // finalAmount = 20000 - 0 - 1000 = 19000
        assertThat(order.getFinalAmount()).isEqualTo(19000L);
    }

    @Test
    @DisplayName("calcFinalAmount 성공 - 배송비 포함")
    void calcFinalAmountSuccess_withDeliveryFee() {
        // given
        Order order = OrderFixture.order(1L, 1000L);
        ProductSku sku = ProductSkuFixture.sku(product(DeliveryType.PARCEL), 10000L, 100);
        OrderItem.create(order, sku, 2); // totalAmount = 20000, 무료배송 기준 미만
        order.calcDeliveryFee();

        // when
        order.calcFinalAmount();

        // then
        // finalAmount = 20000 - 0 - 1000 + 기본 배송비
        assertThat(order.getFinalAmount()).isEqualTo(19000L + DeliveryType.PARCEL.getBaseFee());
    }


    // ========== calcDeliveryFee ==========

    @Test
    @DisplayName("calcDeliveryFee 성공 - 일반배송 상품 합계가 무료배송 기준 미만이면 기본 배송비 1회")
    void calcDeliveryFeeSuccess_parcelBelowThreshold() {
        // given
        Order order = OrderFixture.order(1L);
        Product product = product(DeliveryType.PARCEL);
        OrderItem.create(order, ProductSkuFixture.sku(product, 10000L, 100), 1);
        OrderItem.create(order, ProductSkuFixture.sku(product, 20000L, 100), 1); // 합계 30000

        // when
        order.calcDeliveryFee();

        // then
        assertThat(order.getDeliveryFee()).isEqualTo(DeliveryType.PARCEL.getBaseFee());
    }

    @Test
    @DisplayName("calcDeliveryFee 성공 - 일반배송 상품 합계가 무료배송 기준 이상이면 0")
    void calcDeliveryFeeSuccess_parcelFreeShipping() {
        // given
        Order order = OrderFixture.order(1L);
        Product product = product(DeliveryType.PARCEL);
        OrderItem.create(order, ProductSkuFixture.sku(product, 20000L, 100), 1);
        OrderItem.create(order, ProductSkuFixture.sku(product, 20000L, 100), 1); // 합계 40000

        // when
        order.calcDeliveryFee();

        // then
        assertThat(order.getDeliveryFee()).isZero();
    }

    @Test
    @DisplayName("calcDeliveryFee 성공 - 일반배송과 기사배송이 섞이면 타입별로 계산해 합산")
    void calcDeliveryFeeSuccess_mixedTypes() {
        // given
        Order order = OrderFixture.order(1L);
        OrderItem.create(order, ProductSkuFixture.sku(product(DeliveryType.PARCEL), 10000L, 100), 1);
        OrderItem.create(order, ProductSkuFixture.sku(product(DeliveryType.INSTALLATION), 200000L, 100), 2);

        // when
        order.calcDeliveryFee();

        // then
        long expected = DeliveryType.PARCEL.getBaseFee() + DeliveryType.INSTALLATION.getBaseFee() * 2;
        assertThat(order.getDeliveryFee()).isEqualTo(expected);
    }


    // ========== 헬퍼 메서드 ==========

    private Product product(DeliveryType deliveryType) {
        return ProductFixture.product(CategoryFixture.rootActive(), "A Desk", ProductType.COMPONENT, deliveryType);
    }
}