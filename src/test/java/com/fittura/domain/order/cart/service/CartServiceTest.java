package com.fittura.domain.order.cart.service;

import com.fittura.domain.order.cart.dto.request.CartItemCreateReqDto;
import com.fittura.domain.order.cart.dto.request.CartItemUpdateReqDto;
import com.fittura.domain.order.cart.dto.response.CartItemResDto;
import com.fittura.domain.order.cart.dto.response.CartResDto;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.repository.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository;
import com.fittura.domain.order.cart.support.CartFixture;
import com.fittura.domain.order.cart.support.CartItemFixture;
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
import java.util.Optional;

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
    @DisplayName("빈 장바구니 조회 성공")
    void getCartSuccess_noCart() {
        // given
        Long memberId = 1L;
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when
        CartResDto result = cartService.getCart(memberId);

        // then
        assertThat(result.cartId()).isNull();
        assertThat(result.items()).isEmpty();
        assertThat(result.totalPrice()).isEqualTo(0L);
        verify(cartItemRepository, never()).findCartItemDtosByCart(anyLong());
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 총 금액 합산 반환")
    void getCartSuccess_withItems() {
        // given
        Long memberId = 1L;
        Cart cart = CartFixture.cartWithId(10L, memberId);

        CartItemResDto item1 = new CartItemResDto(
            1L, 1L, "Desk", 1L, "White", "Wood", 10000L, 2, 20000L,
            ProductStatus.ACTIVE, SkuStatus.ACTIVE
        );
        CartItemResDto item2 = new CartItemResDto(
            2L, 2L, "Chair", 2L, "Black", "Metal", 15000L, 1, 15000L,
            ProductStatus.ACTIVE, SkuStatus.ACTIVE
        );

        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartItemRepository.findCartItemDtosByCart(cart.getId())).willReturn(List.of(item1, item2));

        // when
        CartResDto result = cartService.getCart(memberId);

        // then
        assertThat(result.cartId()).isEqualTo(10L);
        assertThat(result.items()).hasSize(2);
        assertThat(result.totalPrice()).isEqualTo(35000L);
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

        given(cartItemRepository.findAllByIdInAndCart_MemberId(itemIds, memberId))
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

        given(cartItemRepository.findAllByIdInAndCart_MemberId(itemIds, memberId))
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

        given(cartItemRepository.findAllByIdInAndCart_MemberId(itemIds, memberId))
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
        CartItemCreateReqDto reqDto = new CartItemCreateReqDto(1L, 3);

        Cart newCart = CartFixture.cart(memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willReturn(newCart);
        given(cartItemRepository.findByCartAndProductSku(newCart, sku)).willReturn(Optional.empty());

        // when
        cartService.createCartItem(memberId, sku, reqDto);

        // then
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 담기 성공 - 기존 장바구니에 새 아이템 추가")
    void createCartItemSuccess_existingCart_newItem() {
        // given
        Long memberId = 1L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithId(1L, product);
        CartItemCreateReqDto reqDto = new CartItemCreateReqDto(1L, 2);

        Cart existingCart = CartFixture.cartWithId(10L, memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(existingCart));
        given(cartItemRepository.findByCartAndProductSku(existingCart, sku)).willReturn(Optional.empty());

        // when
        cartService.createCartItem(memberId, sku, reqDto);

        // then
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 담기 성공 - 기존 아이템에 수량 추가")
    void createCartItemSuccess_existingItem_addQuantity() {
        // given
        Long memberId = 1L;
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.skuWithId(1L, product);
        CartItemCreateReqDto reqDto = new CartItemCreateReqDto(1L, 3);

        Cart existingCart = CartFixture.cartWithId(10L, memberId);
        CartItem existingCartItem = CartItemFixture.cartItem(existingCart, sku, 2);

        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(existingCart));
        given(cartItemRepository.findByCartAndProductSku(existingCart, sku)).willReturn(Optional.of(existingCartItem));

        // when
        cartService.createCartItem(memberId, sku, reqDto);

        // then
        assertThat(existingCartItem.getQuantity()).isEqualTo(5);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository).save(existingCartItem);
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
        List<Long> itemIds = List.of(1L, 2L, 3L);

        // when
        cartService.deleteCartItems(itemIds);

        // then
        verify(cartItemRepository).deleteAllById(itemIds);
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
}