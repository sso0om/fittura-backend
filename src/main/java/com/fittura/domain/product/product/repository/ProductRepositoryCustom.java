package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.dto.response.ProductResDto;

import java.util.Optional;

public interface ProductRepositoryCustom {
    Optional<ProductResDto> findWithDetailById(Long id);
}
