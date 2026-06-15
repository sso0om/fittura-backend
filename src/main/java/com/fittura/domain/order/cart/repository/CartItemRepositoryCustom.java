package com.fittura.domain.order.cart.repository;

import com.fittura.domain.order.cart.dto.response.CartItemResDto;

import java.util.List;

public interface CartItemRepositoryCustom {
    List<CartItemResDto> findCartItemDtosByCart(Long cartId);
}
