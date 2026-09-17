package com.fittura.domain.order.facade;

import com.fittura.domain.order.cart.dto.request.CartItemCreateReqDto;
import com.fittura.domain.order.cart.dto.request.CartItemUpdateReqDto;
import com.fittura.domain.order.cart.dto.response.CartResDto;
import com.fittura.domain.order.cart.service.CartService;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartFacade {

    private final CartService cartService;
    private final SkuService skuService;

    @Transactional(readOnly = true)
    public CartResDto getCart(Long memberId) {
        return cartService.getCart(memberId);
    }

    @Transactional
    public void createCartItems(Long memberId, List<CartItemCreateReqDto> reqDto) {
        Map<Long, Integer> quantityBySkuId = toQuantityBySkuId(reqDto);
        List<ProductSku> skus = skuService.getSkusById(quantityBySkuId.keySet());
        cartService.addCartItems(memberId, skus, quantityBySkuId);
    }

    @Transactional
    public void updateCartItem(Long memberId, Long itemId, CartItemUpdateReqDto reqDto) {
        cartService.updateCartItem(memberId, itemId, reqDto);
    }

    @Transactional
    public void deleteCartItem(Long memberId, Long itemId) {
        cartService.deleteCartItem(memberId, itemId);
    }


    // ========== 헬퍼 메서드 ==========

    private Map<Long, Integer> toQuantityBySkuId(List<CartItemCreateReqDto> reqDto) {
        return reqDto.stream()
            .collect(Collectors.toMap(
                CartItemCreateReqDto::skuId,
                CartItemCreateReqDto::quantity
            ));
    }
}
