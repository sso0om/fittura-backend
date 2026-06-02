package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.entity.ProductComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompositionRepository extends JpaRepository<ProductComposition, Long>, CompositionRepositoryCustom {
    public List<ProductComposition> findByParentProductId(Long productId);
}
