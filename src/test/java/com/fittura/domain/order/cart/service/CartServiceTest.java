package com.fittura.domain.order.cart.service;

import com.fittura.domain.order.cart.dto.request.CartItemCreateReqDto;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.repository.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository;
import com.fittura.domain.order.cart.support.CartFixture;
import com.fittura.domain.order.cart.support.CartItemFixture;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        verify(cartItemRepository).save(existingCartItem);
    }
}