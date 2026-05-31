package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.dto.response.ProductWithSkuResDto;
import com.fittura.domain.product.product.dto.response.ProductWithAllResDto;

import java.util.Optional;

public interface ProductRepositoryCustom {
    Optional<ProductWithAllResDto> findWithAllById(Long id);
    Optional<ProductWithSkuResDto> findWithSkuById(Long id);
}
