package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
    List<ProductSku> findByProductIdAndStatusNot(Long productId, SkuStatus status);

    Optional<ProductSku> findByIdAndStatusNot(Long skuId, SkuStatus status);

    boolean existsByProductIdAndId(Long productId, Long skuId);

    @Modifying
    @Query("update ProductSku s set s.stockQuantity = s.stockQuantity + :restoreQuantity where s.id = :skuId")
    void restoreStock(@Param("skuId") Long skuId, @Param("restoreQuantity") Integer restoreQuantity);
}
