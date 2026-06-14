package com.fittura.domain.order.cart.service;

import com.fittura.domain.order.cart.dto.request.CartItemCreateReqDto;
import com.fittura.domain.order.cart.dto.response.CartItemResDto;
import com.fittura.domain.order.cart.dto.response.CartResDto;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.repository.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository;
import com.fittura.domain.product.sku.entity.ProductSku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartResDto getCart(Long memberId) {
        Optional<Cart> cart = getOpCart(memberId);
        if (cart.isEmpty()) {
            return CartResDto.empty();
        }

        List<CartItemResDto> items = cartItemRepository.findCartItemDtosByCart(cart.get().getId());
        Long totalPrice = items.stream()
            .mapToLong(CartItemResDto::itemTotalPrice)
            .sum();

        return CartResDto.from(cart.get(), items, totalPrice);
    }

    public void createCartItem(Long memberId, ProductSku sku, CartItemCreateReqDto reqDto) {
        Cart cart = getCartOrCreate(memberId);

        Optional<CartItem> opCartItem = getOpCartItem(cart, sku);
        CartItem cartItem;
        if (opCartItem.isPresent()) {
            cartItem = opCartItem.get();
            cartItem.addQuantity(reqDto.quantity());
        } else {
            cartItem = CartItem.create(cart, sku, reqDto.quantity());
        }
        cartItemRepository.save(cartItem);
    }


    // ========== 헬퍼 메서드 ==========

    private Optional<Cart> getOpCart(Long memberId) {
        return cartRepository.findByMemberId(memberId);
    }

    private Cart getCartOrCreate(Long memberId) {
        return cartRepository.findByMemberId(memberId)
            .orElseGet(() -> cartRepository.save(Cart.create(memberId)));
    }

    private Optional<CartItem> getOpCartItem(Cart cart, ProductSku sku) {
        return cartItemRepository.findByCartAndProductSku(cart, sku);
    }
}
