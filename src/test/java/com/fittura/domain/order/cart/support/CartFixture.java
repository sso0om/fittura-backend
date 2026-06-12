package com.fittura.domain.order.cart.support;

import com.fittura.domain.order.cart.entity.Cart;
import org.springframework.test.util.ReflectionTestUtils;

public class CartFixture {

    private CartFixture() {}

    public static Cart cart(Long memberId) {
        return Cart.create(memberId);
    }

    public static Cart cartWithId(Long id, Long memberId) {
        Cart cart = Cart.create(memberId);
        ReflectionTestUtils.setField(cart, "id", id);
        return cart;
    }
}
