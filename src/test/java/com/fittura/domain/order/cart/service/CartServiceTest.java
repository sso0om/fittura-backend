package com.fittura.domain.order.cart.service;

import com.fittura.domain.order.cart.dto.request.CartItemUpdateReqDto;
import com.fittura.domain.order.cart.dto.response.CartItemResDto;
import com.fittura.domain.order.cart.dto.response.CartResDto;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.repository.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository;
import com.fittura.domain.order.cart.support.CartFixture;
import com.fittura.domain.order.cart.support.CartItemFixture;
import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.constant.SkuStatus;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    // ========== 장바구니 조회 ==========

    @Test
    @DisplayName("빈 장바구니 조회 성공 - 장바구니 없으면 아이템 조회하지 않음")
    void getCartSuccess_noCart() {
        // given
        Long memberId = 1L;
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when
        CartResDto result = cartService.getCart(memberId);

        // then
        assertThat(result.cartId()).isNull();
        assertThat(result.items()).isEmpty();
        verify(cartItemRepository, never()).findCartItemDtosByCart(anyLong());
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 조회된 아이템을 순서 그대로 반환")
    void getCartSuccess_withItems() {
        // given
        Long memberId = 1L;
        Cart cart = CartFixture.cartWithId(10L, memberId);

        CartItemResDto item1 = cartItemResDto(1L, DeliveryType.PARCEL, 10_000L, null, 2);
        CartItemResDto item2 = cartItemResDto(2L, DeliveryType.INSTALLATION, 15_000L, null, 1);

        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartItemRepository.findCartItemDtosByCart(cart.getId())).willReturn(List.of(item1, item2));

        // when
        CartResDto result = cartService.getCart(memberId);

        // then
        assertThat(result.cartId()).isEqualTo(10L);
        assertThat(result.items()).containsExactly(item1, item2);
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 장바구니는 있으나 조회 가능한 아이템 없음")
    void getCartSuccess_cartWithoutItems() {
        // given
        Long memberId = 1L;
        Cart cart = CartFixture.cartWithId(10L, memberId);

        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartItemRepository.findCartItemDtosByCart(cart.getId())).willReturn(List.of());

        // when
        CartResDto result = cartService.getCart(memberId);

        // then
        assertThat(result.cartId()).isEqualTo(10L);
        assertThat(result.items()).isEmpty();
    }


    // ========== 장바구니 아이템 다건 조회 ==========

    @Test
    @DisplayName("장바구니 아이템 다건 조회 성공")
    void getItemsByIdAndMemberSuccess() {
        // given
        Long memberId = 1L;
        List<Long> itemIds = List.of(1L, 2L);
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithId(1L, product);
        Cart cart = CartFixture.cartWithId(10L, memberId);
        CartItem item1 = CartItemFixture.cartItemWithId(1L, cart, sku, 2);
        CartItem item2 = CartItemFixture.cartItemWithId(2L, cart, sku, 1);

        given(cartItemRepository.findAllWithSkuForUpdate(itemIds, memberId))
            .willReturn(List.of(item1, item2));

        // when
        List<CartItem> result = cartService.getItemsByIdAndMember(itemIds, memberId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(item1, item2);
    }

    @Test
    @DisplayName("장바구니 아이템 다건 조회 실패 - 일부 아이템이 해당 회원 것이 아님")
    void getItemsByIdAndMemberFail_partialMatch() {
        // given
        Long memberId = 1L;
        List<Long> itemIds = List.of(1L, 2L);
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithId(1L, product);
        Cart cart = CartFixture.cartWithId(10L, memberId);
        CartItem item1 = CartItemFixture.cartItemWithId(1L, cart, sku, 2);

        given(cartItemRepository.findAllWithSkuForUpdate(itemIds, memberId))
            .willReturn(List.of(item1));

        // when & then
        assertThatThrownBy(() -> cartService.getItemsByIdAndMember(itemIds, memberId))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("장바구니 아이템 다건 조회 실패 - 아이템 없음")
    void getItemsByIdAndMemberFail_notFound() {
        // given
        Long memberId = 1L;
        List<Long> itemIds = List.of(999L);

        given(cartItemRepository.findAllWithSkuForUpdate(itemIds, memberId))
            .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> cartService.getItemsByIdAndMember(itemIds, memberId))
            .isInstanceOf(ServiceException.class);
    }


    // ========== 장바구니 아이템 생성 ==========

    @Test
    @DisplayName("장바구니 담기 성공 - 장바구니 없음: 새 장바구니 생성 후 아이템 추가")
    void createCartItemSuccess_noCart() {
        // given
        Long memberId = 1L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithId(1L, product);

        Cart newCart = CartFixture.cart(memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willReturn(newCart);
        given(cartItemRepository.findAllByCartAndSkuIdIn(newCart, List.of(sku.getId()))).willReturn(List.of());

        // when
        cartService.addCartItems(memberId, List.of(sku), Map.of(sku.getId(), 3));

        // then
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).saveAll(any());
    }

    @Test
    @DisplayName("장바구니 담기 성공 - 기존 장바구니에 새 아이템 추가")
    void createCartItemSuccess_existingCart_newItem() {
        // given
        Long memberId = 1L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithId(1L, product);

        Cart existingCart = CartFixture.cartWithId(10L, memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(existingCart));
        given(cartItemRepository.findAllByCartAndSkuIdIn(existingCart, List.of(sku.getId()))).willReturn(List.of());

        // when
        cartService.addCartItems(memberId, List.of(sku), Map.of(sku.getId(), 2));

        // then
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository).saveAll(any());
    }

    @Test
    @DisplayName("장바구니 담기 성공 - 기존 아이템에 수량 추가")
    void createCartItemSuccess_existingItem_addQuantity() {
        // given
        Long memberId = 1L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithId(1L, product);

        Cart existingCart = CartFixture.cartWithId(10L, memberId);
        CartItem existingCartItem = CartItemFixture.cartItem(existingCart, sku, 2);

        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(existingCart));
        given(cartItemRepository.findAllByCartAndSkuIdIn(existingCart, List.of(sku.getId()))).willReturn(List.of(existingCartItem));

        // when
        cartService.addCartItems(memberId, List.of(sku), Map.of(sku.getId(), 3));

        // then
        assertThat(existingCartItem.getQuantity()).isEqualTo(5);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, never()).saveAll(any());
    }


    // ========== 장바구니 아이템 수량 수정 ==========

    @Test
    @DisplayName("장바구니 아이템 수량 수정 성공")
    void updateCartItemSuccess() {
        // given
        Long memberId = 1L;
        Long itemId = 10L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithId(1L, product);
        Cart cart = CartFixture.cartWithId(100L, memberId);
        CartItem cartItem = CartItemFixture.cartItemWithId(itemId, cart, sku, 2);
        CartItemUpdateReqDto reqDto = new CartItemUpdateReqDto(7);

        given(cartItemRepository.findByIdAndCart_MemberId(itemId, memberId)).willReturn(Optional.of(cartItem));

        // when
        cartService.updateCartItem(memberId, itemId, reqDto);

        // then
        assertThat(cartItem.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정 실패 - 아이템 없음")
    void updateCartItemFail_notFoundItem() {
        // given
        Long memberId = 1L;
        Long itemId = 999L;
        CartItemUpdateReqDto reqDto = new CartItemUpdateReqDto(5);

        given(cartItemRepository.findByIdAndCart_MemberId(itemId, memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.updateCartItem(memberId, itemId, reqDto))
            .isInstanceOf(ServiceException.class);
    }


    // ========== 장바구니 아이템 삭제 ==========

    @Test
    @DisplayName("장바구니 아이템 일괄 삭제 성공")
    void deleteCartItemsSuccess() {
        // given
        Long memberId = 1L;
        Set<Long> skuIds = Set.of(1L, 2L);

        // when
        cartService.deleteCartItems(memberId, skuIds);

        // then
        verify(cartItemRepository).deleteByMemberIdAndSkuIds(memberId, skuIds);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 성공")
    void deleteCartItemSuccess() {
        // given
        Long memberId = 1L;
        Long itemId = 10L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithId(1L, product);
        Cart cart = CartFixture.cartWithId(100L, memberId);
        CartItem cartItem = CartItemFixture.cartItemWithId(itemId, cart, sku, 2);

        given(cartItemRepository.findByIdAndCart_MemberId(itemId, memberId)).willReturn(Optional.of(cartItem));

        // when
        cartService.deleteCartItem(memberId, itemId);

        // then
        verify(cartItemRepository).deleteById(itemId);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 실패 - 아이템 없음")
    void deleteCartItemFail_notFoundItem() {
        // given
        Long memberId = 1L;
        Long itemId = 999L;

        given(cartItemRepository.findByIdAndCart_MemberId(itemId, memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.deleteCartItem(memberId, itemId))
            .isInstanceOf(ServiceException.class);
    }


    // ========== 헬퍼 메서드 ==========

    private CartItemResDto cartItemResDto(
        Long id, DeliveryType deliveryType, Long price, Long salePrice, Integer quantity
    ) {
        return new CartItemResDto(
            id, id, "A Desk", null,
            ProductStatus.ACTIVE, deliveryType,
            id, "White", "Wood",
            price, salePrice, quantity, SkuStatus.ACTIVE
        );
    }
}
