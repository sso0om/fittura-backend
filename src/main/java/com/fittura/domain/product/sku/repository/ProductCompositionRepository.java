package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.entity.ProductComposition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCompositionRepository extends JpaRepository<ProductComposition, Long> {
}
