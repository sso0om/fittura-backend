package com.fittura.domain.order.facade.CartFacade;

import com.fittura.domain.order.cart.dto.request.CartItemCreateReqDto;
import com.fittura.domain.order.cart.service.CartService;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CartFacade {

    private final CartService cartService;
    private final SkuService skuService;

    @Transactional
    public void createCartItem(Long memberId, CartItemCreateReqDto reqDto) {
        ProductSku productSku = skuService.getProductSku(reqDto.skuId());
        cartService.createCartItem(memberId, productSku, reqDto);
    }
}
