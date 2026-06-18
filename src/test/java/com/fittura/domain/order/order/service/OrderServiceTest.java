package com.fittura.domain.order.order.service;

import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.support.CartFixture;
import com.fittura.domain.order.cart.support.CartItemFixture;
import com.fittura.domain.order.order.dto.request.AddressCreateReqDto;
import com.fittura.domain.order.order.dto.request.OrderCreateReqDto;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.entity.OrderAddress;
import com.fittura.domain.order.order.entity.OrderItem;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.repository.OrderAddressRepository;
import com.fittura.domain.order.order.repository.OrderItemRepository;
import com.fittura.domain.order.order.repository.OrderRepository;
import com.fittura.domain.order.order.support.OrderAddressFixture;
import com.fittura.domain.order.order.support.OrderFixture;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import com.fittura.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository itemRepository;

    @Mock
    private OrderAddressRepository addressRepository;

    @InjectMocks
    private OrderService orderService;


    // ========== 주문 생성 ==========

    @Test
    @DisplayName("주문 생성 성공")
    void createOrderSuccess() {
        // given
        Long memberId = 1L;
        OrderCreateReqDto reqDto = new OrderCreateReqDto(List.of(1L), 1000L, OrderAddressFixture.addressReqDto());
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        Order result = orderService.createOrder(memberId, reqDto);

        // then
        assertThat(result.getMemberId()).isEqualTo(memberId);
        assertThat(result.getPointUsedAmount()).isEqualTo(1000L);
        verify(orderRepository).save(any(Order.class));
    }


    // ========== 금액 계산 ==========

    @Test
    @DisplayName("금액 계산 성공 - finalAmount 계산됨")
    void calcAmountSuccess() {
        // given
        Order order = OrderFixture.order(1L, 500L);
        // totalAmount=0, discountAmount=0, pointUsedAmount=500, deliveryFee=4000
        // finalAmount = 0 - 0 - 500 + 4000 = 3500

        // when
        orderService.calcAmount(order);

        // then
        assertThat(order.getFinalAmount()).isEqualTo(3500L);
    }


    // ========== 주문 아이템 생성 ==========

    @Test
    @DisplayName("주문 아이템 생성 성공")
    void createOrderItemSuccess() {
        // given
        Long memberId = 1L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 20000L, 10);
        Cart cart = CartFixture.cart(memberId);
        CartItem cartItem = CartItemFixture.cartItem(cart, sku, 3);
        Order order = OrderFixture.order(memberId);

        // when
        orderService.createOrderItem(cartItem, order);

        // then
        assertThat(sku.getReservedQuantity()).isEqualTo(3);
        verify(itemRepository).save(any(OrderItem.class));
    }

    @Test
    @DisplayName("주문 아이템 생성 실패 - 비활성 SKU")
    void createOrderItemFail_skuNotActive() {
        // given
        Long memberId = 1L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 20000L, 10);
        sku.soldOut();
        Order order = OrderFixture.order(memberId);
        Cart cart = CartFixture.cart(memberId);
        CartItem cartItem = CartItemFixture.cartItem(cart, sku, 3);

        // when & then
        assertThatThrownBy(() -> orderService.createOrderItem(cartItem, order))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(OrderErrorCode.SKU_MUST_ACTIVE);
    }

    @Test
    @DisplayName("주문 아이템 생성 실패 - 재고 부족")
    void createOrderItemFail_stockNotValid() {
        // given
        Long memberId = 1L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 20000L, 2);
        Cart cart = CartFixture.cart(memberId);
        CartItem cartItem = CartItemFixture.cartItem(cart, sku, 5);
        Order order = OrderFixture.order(memberId);

        // when & then
        assertThatThrownBy(() -> orderService.createOrderItem(cartItem, order))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(OrderErrorCode.STOCK_NOT_VALID);
    }


    // ========== 주문 주소 생성 ==========

    @Test
    @DisplayName("주문 주소 생성 성공")
    void createOrderAddressSuccess() {
        // given
        Order order = OrderFixture.order(1L);
        AddressCreateReqDto reqDto = OrderAddressFixture.addressReqDto();

        // when
        orderService.createOrderAddress(order, reqDto);

        // then
        verify(addressRepository).save(any(OrderAddress.class));
    }
}