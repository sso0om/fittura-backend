package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
    @Query("""
        SELECT s FROM ProductSku s
        WHERE s.product.id = :productId
        AND s.status != com.fittura.domain.product.sku.constant.SkuStatus.ARCHIVED
        """)
    List<ProductSku> findByProductId(@Param("productId") Long productId);

    boolean existsByIdAndProductId(Long productId, Long skuId);
}
