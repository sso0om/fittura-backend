package com.fittura.domain.order.cart.repository;

import com.fittura.domain.order.cart.dto.response.CartItemResDto;
import com.fittura.domain.order.cart.entity.CartItem;

import java.util.List;

public interface CartItemRepositoryCustom {
    List<CartItem> findAllWithSkuForUpdate(List<Long> itemIds, Long memberId);
    List<CartItemResDto> findCartItemDtosByCart(Long cartId);
}
