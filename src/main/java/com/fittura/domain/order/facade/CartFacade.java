package com.fittura.domain.order.facade;

import com.fittura.domain.order.cart.dto.request.CartItemCreateReqDto;
import com.fittura.domain.order.cart.dto.response.CartResDto;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.service.CartService;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    public void createCartItem(Long memberId, CartItemCreateReqDto reqDto) {
        ProductSku productSku = skuService.getProductSku(reqDto.skuId());
        cartService.createCartItem(memberId, productSku, reqDto);
    }
}
