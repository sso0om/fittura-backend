package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.entity.Product;

import java.util.Optional;

public interface ProductRepositoryCustom {
    Optional<Product> findWithDetailById(Long id);
}
