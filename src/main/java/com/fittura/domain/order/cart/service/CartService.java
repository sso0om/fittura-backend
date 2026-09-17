package com.fittura.domain.order.cart.service;

import com.fittura.domain.order.cart.dto.request.CartItemUpdateReqDto;
import com.fittura.domain.order.cart.dto.response.CartItemResDto;
import com.fittura.domain.order.cart.dto.response.CartResDto;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.error.CartErrorCode;
import com.fittura.domain.order.cart.repository.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // ========== 장바구니 ==========

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


    // ========== 장바구니 제품 ==========

    public List<CartItem> getItemsByIdAndMember(List<Long> itemIds, Long memberId) {
        List<Long> distinctIds = itemIds.stream().distinct().toList();
        List<CartItem> cartItems = cartItemRepository.findAllWithSkuForUpdate(distinctIds, memberId);

        if (cartItems.size() != distinctIds.size()) {
            throw new ServiceException(CartErrorCode.NOT_FOUND_ITEM);
        }
        return cartItems;
    }

    public void addCartItems(Long memberId, List<ProductSku> skus, Map<Long, Integer> quantityBySkuId) {
        Cart cart = getCartOrCreate(memberId);

        List<Long> skuIds = skus.stream()
            .map(ProductSku::getId)
            .toList();

        Map<Long, CartItem> existingItemBySkuId = cartItemRepository.findAllByCartAndSkuIdIn(cart, skuIds).stream()
            .collect(Collectors.toMap(
                item -> item.getProductSku().getId(),
                Function.identity()
            ));

        List<CartItem> itemsToSave = new ArrayList<>();
        for (ProductSku sku : skus) {
            Integer quantity = quantityBySkuId.get(sku.getId());
            CartItem item = existingItemBySkuId.get(sku.getId());

            if (item != null) {
                item.addQuantity(quantity);
            } else {
                itemsToSave.add(CartItem.create(cart, sku, quantity));
            }
        }

        if (!itemsToSave.isEmpty()) {
            cartItemRepository.saveAll(itemsToSave);
        }
    }

    public void updateCartItem(Long memberId, Long itemId, CartItemUpdateReqDto reqDto) {
        CartItem cartitem = getItemByIdAndMember(itemId, memberId);
        cartitem.changeQuantity(reqDto.quantity());
    }

    public void deleteCartItems(Long memberId, Set<Long> skuIds) {
        cartItemRepository.deleteByMemberIdAndSkuIds(memberId, skuIds);
    }

    public void deleteCartItem(Long memberId, Long itemId) {
        CartItem item = getItemByIdAndMember(itemId, memberId);
        cartItemRepository.deleteById(item.getId());
    }


    // ========== 헬퍼 메서드 ==========

    private Optional<Cart> getOpCart(Long memberId) {
        return cartRepository.findByMemberId(memberId);
    }

    private Cart getCartOrCreate(Long memberId) {
        return cartRepository.findByMemberId(memberId)
            .orElseGet(() -> cartRepository.save(Cart.create(memberId)));
    }

    private Optional<CartItem> getOpItemByCartAndSku(Cart cart, ProductSku sku) {
        return cartItemRepository.findByCartAndProductSku(cart, sku);
    }

    private CartItem getItemByIdAndMember(Long itemId, Long memberId) {
        return cartItemRepository.findByIdAndCart_MemberId(itemId, memberId)
            .orElseThrow(() -> new ServiceException(CartErrorCode.NOT_FOUND_ITEM));
    }
}
