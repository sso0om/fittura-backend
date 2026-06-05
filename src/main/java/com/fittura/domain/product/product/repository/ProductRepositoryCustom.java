package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.dto.request.ProductSearchCondition;
import com.fittura.domain.product.product.dto.response.ProductWithAllResDto;
import com.fittura.domain.product.product.dto.response.ProductWithSkuResDto;
import com.fittura.domain.product.product.dto.response.ProductResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepositoryCustom {
    Page<ProductResDto> findProducts(ProductSearchCondition condition, Pageable pageable);
    Optional<ProductWithAllResDto> findWithAllById(Long id);
    Optional<ProductWithSkuResDto> findWithSkuById(Long id);
}
