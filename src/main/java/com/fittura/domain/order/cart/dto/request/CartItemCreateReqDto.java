package com.fittura.domain.order.cart.dto.request;

public record CartItemCreateReqDto(
    Long skuId,
    Integer quantity
) {
}
