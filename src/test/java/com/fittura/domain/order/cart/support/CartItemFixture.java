package com.fittura.domain.order.cart.support;

import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.test.util.ReflectionTestUtils;

public class CartItemFixture {

    private CartItemFixture() {}

    public static CartItem cartItem(Cart cart, ProductSku sku, Integer quantity) {
        return CartItem.create(cart, sku, quantity);
    }

    public static CartItem cartItemWithId(Long id, Cart cart, ProductSku sku, Integer quantity) {
        CartItem cartItem = CartItem.create(cart, sku, quantity);
        ReflectionTestUtils.setField(cartItem, "id", id);
        return cartItem;
    }
}
