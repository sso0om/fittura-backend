package com.fittura.domain.order.cart.service.CartService;

import com.fittura.domain.order.cart.dto.request.CartItemCreateReqDto;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.repository.CartItemRepostiory.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository.CartRepository;
import com.fittura.domain.product.sku.entity.ProductSku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

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

    private Cart getCartOrCreate(Long memberId) {
        return cartRepository.findByMemberId(memberId)
            .orElseGet(() -> cartRepository.save(Cart.create(memberId)));
    }

    private Optional<CartItem> getOpCartItem(Cart cart, ProductSku sku) {
        return cartItemRepository.findByCartAndProductSku(cart, sku);
    }
}
