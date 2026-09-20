package com.fittura.domain.order.cart.dto.response;

import com.fittura.domain.order.cart.entity.Cart;

import java.util.List;

public record CartResDto(
    Long cartId,
    List<CartItemResDto> items
) {
    public static CartResDto empty() {
        return new CartResDto(
            null,
            List.of()
        );
    }

    public static CartResDto from(Cart cart, List<CartItemResDto> items) {
        return new CartResDto(
            cart.getId(),
            items
        );
    }
}
