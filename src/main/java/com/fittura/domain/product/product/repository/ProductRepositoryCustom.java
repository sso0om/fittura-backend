package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.dto.response.ProductResDto;
import com.fittura.domain.product.product.dto.response.ProductWithStockResDto;

import java.util.Optional;

public interface ProductRepositoryCustom {
    Optional<ProductWithStockResDto> findWithStockById(Long id);
    Optional<ProductResDto> findWithDetailById(Long id);
}
