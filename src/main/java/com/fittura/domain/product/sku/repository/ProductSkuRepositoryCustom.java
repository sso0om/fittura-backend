package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.dto.response.SkuResDto;

import java.util.List;

public interface ProductSkuRepositoryCustom {
    List<SkuResDto> findSkuDtosByProductId(Long productId);
}
