package com.fittura.domain.order.cart.entity;

import com.fittura.domain.order.cart.support.CartFixture;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemTest {

    // ========== CartItem 생성 ==========

    @Test
    @DisplayName("CartItem 생성 성공")
    void createSuccess() {
        // given
        Cart cart = CartFixture.cart(1L);
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 10000L, 100);

        // when
        CartItem cartItem = CartItem.create(cart, sku, 2);

        // then
        assertThat(cartItem.getQuantity()).isEqualTo(2);
    }

    // ========== addQuantity ==========

    @Test
    @DisplayName("addQuantity 성공 - 이미 장바구니에 존재할 경우 기존 수량에 1 증가")
    void addQuantitySuccess() {
        // given
        Cart cart = CartFixture.cart(1L);
        Product product = ProductFixture.component("A Desk");
        ProductSku sku = ProductSkuFixture.sku(product, 10000L, 100);
        CartItem cartItem = CartItem.create(cart, sku, 2);

        // when
        cartItem.addQuantity(3);

        // then
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }
}