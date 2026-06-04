package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.product.dto.response.CompositionResDto;

import java.util.List;

public interface CompositionRepositoryCustom {
    List<CompositionResDto> findCompositionDtosByProductId(Long productId);
}
