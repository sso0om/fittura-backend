package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
    List<ProductSku> findByProductIdAndStatusNot(Long productId, SkuStatus status);

    Optional<ProductSku> findByIdAndStatusNot(Long skuId, SkuStatus status);

    boolean existsByIdAndProductId(Long productId, Long skuId);
}
