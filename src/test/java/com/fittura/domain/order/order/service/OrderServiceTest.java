package com.fittura.domain.order.order.service;

import com.fittura.domain.member.address.entity.MemberAddress;
import com.fittura.domain.member.address.support.MemberAddressFixture;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.support.CartFixture;
import com.fittura.domain.order.cart.support.CartItemFixture;
import com.fittura.domain.order.order.constant.OrderStatus;
import com.fittura.domain.order.order.dto.request.CartOrderCreateReqDto;
import com.fittura.domain.order.order.dto.response.OrderAddressResDto;
import com.fittura.domain.order.order.dto.request.OrderSkuReqDto;
import com.fittura.domain.order.order.dto.response.OrderWithAllResDto;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.entity.OrderAddress;
import com.fittura.domain.order.order.entity.OrderItem;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.repository.OrderAddressRepository;
import com.fittura.domain.order.order.repository.OrderItemRepository;
import com.fittura.domain.order.order.repository.OrderRepository;
import com.fittura.domain.order.order.support.OrderFixture;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import com.fittura.global.error.ItemError;
import com.fittura.global.exception.ServiceException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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

    // ========== 주문 조회 ==========

    @Test
    @DisplayName("주문 조회 성공")
    void getOrderDetailSuccess() {
        // given
        Long orderId = 1L;
        Long memberId = 1L;
        OrderWithAllResDto dto = new OrderWithAllResDto(
            orderId, "20260712-abcd1234", OrderStatus.PENDING, LocalDateTime.now(),
            20000L, 0L, 1000L, 4000L, 23000L,
            new OrderAddressResDto(
                "홍길동", "01012341234", "12345",
                "서울특별시 중구 서소문로 127", null, "서울특별시", "중구", null
            ),
            List.of()
        );
        given(orderRepository.findWithAllByIdAndMemberId(orderId, memberId))
            .willReturn(Optional.of(dto));

        // when
        OrderWithAllResDto result = orderService.getOrderDetail(orderId, memberId);

        // then
        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("주문 조회 실패 - 존재하지 않거나 본인 주문 아님")
    void getOrderDetailFail_notFound() {
        // given
        Long orderId = 999L;
        Long memberId = 1L;
        given(orderRepository.findWithAllByIdAndMemberId(orderId, memberId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetail(orderId, memberId))
            .isInstanceOf(ServiceException.class)
            .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                .isEqualTo(OrderErrorCode.NOT_FOUND_ORDER));
    }


    // ========== 주문 생성 ==========

    @Test
    @DisplayName("주문 생성 성공")
    void createOrderCartSuccess() {
        // given
        Long memberId = 1L;
        CartOrderCreateReqDto reqDto = new CartOrderCreateReqDto(List.of(1L), 1000L, 1L, null);
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        Order result = orderService.createOrderCart(memberId, reqDto);

        // then
        assertThat(result.getMemberId()).isEqualTo(memberId);
        assertThat(result.getPointUsedAmount()).isEqualTo(1000L);
        verify(orderRepository).save(any(Order.class));
    }


    // TODO: ========== 금액 계산 ==========

    // ========== 주문 아이템 생성 ==========

    @Test
    @DisplayName("주문 아이템 생성 성공")
    void createOrderCartItemSuccess() {
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


    // ========== 주문 주소 생성 ==========

    @Test
    @DisplayName("주문 주소 생성 성공 - 배송지 정보를 복사하고 배송 메모는 요청 값 사용")
    void createOrderCartAddressSuccess() {
        // given
        Order order = OrderFixture.order(1L);
        MemberAddress memberAddress = MemberAddressFixture.address(1L, true);
        String deliveryMemo = "문앞에 놓아주세요.";
        ArgumentCaptor<OrderAddress> captor = ArgumentCaptor.forClass(OrderAddress.class);

        // when
        orderService.createOrderAddress(order, memberAddress, deliveryMemo);

        // then
        verify(addressRepository).save(captor.capture());
        OrderAddress saved = captor.getValue();
        assertThat(saved.getOrder()).isEqualTo(order);
        assertThat(saved).extracting(
            OrderAddress::getReceiverName, OrderAddress::getPhoneNumber,
            OrderAddress::getZipCode, OrderAddress::getAddress,
            OrderAddress::getAddressDetail, OrderAddress::getSido,
            OrderAddress::getSigungu, OrderAddress::getDeliveryMemo
        ).containsExactly(
            memberAddress.getReceiverName(), memberAddress.getPhoneNumber(),
            memberAddress.getZipCode(), memberAddress.getAddress(),
            memberAddress.getAddressDetail(), memberAddress.getSido(),
            memberAddress.getSigungu(), deliveryMemo
        );
    }


    // ========== 바로 주문 SKU 중복 검사 ==========

    @Test
    @DisplayName("SKU 중복 검사 성공 - 서로 다른 SKU")
    void validateNoDuplicateSkuSuccess() {
        // given
        List<OrderSkuReqDto> orderSkus = List.of(
            new OrderSkuReqDto(1L, 2),
            new OrderSkuReqDto(2L, 2)
        );

        // when & then
        assertThatCode(() -> orderService.validateNoDuplicateSku(orderSkus))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SKU 중복 검사 실패 - 같은 SKU가 수량만 다르게 중복")
    void validateNoDuplicateSkuFail_sameSkuDifferentQuantity() {
        // given
        List<OrderSkuReqDto> orderSkus = List.of(
            new OrderSkuReqDto(1L, 2),
            new OrderSkuReqDto(1L, 3)
        );

        // when & then
        assertThatThrownBy(() -> orderService.validateNoDuplicateSku(orderSkus))
            .isInstanceOf(ServiceException.class)
            .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                .isEqualTo(OrderErrorCode.DUPLICATE_SKU));
    }


    // ========== 바로 주문 SKU 검증 ==========

    @Test
    @DisplayName("바로 주문 SKU 검증 성공")
    void validateDirectSkusSuccess() {
        // given
        ProductSku sku = ProductSkuFixture.skuWithId(1L, activeProduct(), 10_000L);
        ProductSku otherSku = ProductSkuFixture.skuWithId(2L, activeProduct(), 12_000L);

        // when & then
        assertThatCode(() -> orderService.validateDirectSkus(
            List.of(sku, otherSku),
            Map.of(1L, 5, 2L, 50)   // 재고 50 → 경계값(재고와 같은 수량)까지 허용
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("바로 주문 SKU 검증 실패 - 항목마다 사유를 모아서 반환")
    void validateDirectSkusFail_collectsErrorsPerItem() {
        // given
        ProductSku pausedSku = ProductSkuFixture.skuWithId(1L, activeProduct(), 10_000L);
        pausedSku.pause();
        ProductSku disabledProductSku = ProductSkuFixture.skuWithId(
            2L, ProductFixture.componentWithId(11L, "B Desk"), 10_000L);   // 상품 미활성(DISABLED)
        ProductSku lowStockSku = ProductSkuFixture.skuWithId(3L, activeProduct(), 10_000L);   // 재고 50

        // when & then
        assertThatThrownBy(() -> orderService.validateDirectSkus(
            List.of(pausedSku, disabledProductSku, lowStockSku),
            Map.of(1L, 1, 2L, 1, 3L, 51)
        ))
            .isInstanceOf(ServiceException.class)
            .satisfies(e -> {
                ServiceException exception = (ServiceException) e;
                assertThat(exception.getErrorCode()).isEqualTo(OrderErrorCode.DIRECT_SKUS_NOT_VALID);
                assertThat(exception.getDetail())
                    .asInstanceOf(InstanceOfAssertFactories.list(ItemError.class))
                    .extracting(ItemError::code)
                    .containsExactly(
                        OrderErrorCode.SKU_MUST_ACTIVE.getCode(),
                        OrderErrorCode.PRODUCT_MUST_ACTIVE.getCode(),
                        OrderErrorCode.STOCK_NOT_VALID.getCode());
            });
    }


    // ========== 헬퍼 메서드 ==========

    private Product activeProduct() {
        Product product = ProductFixture.componentWithId(10L, "A Desk");
        product.activate();
        return product;
    }
}