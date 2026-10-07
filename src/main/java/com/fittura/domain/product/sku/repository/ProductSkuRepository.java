package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.entity.ProductSku;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long>, ProductSkuRepositoryCustom {

    @EntityGraph(attributePaths = "product")
    List<ProductSku> findAllByIdInAndStatusNot(Set<Long> skuIds, SkuStatus skuStatus);

    @EntityGraph(attributePaths = {"product", "product.mainImage", "color", "material"})
    List<ProductSku> findAllWithDetailByIdInAndStatusNot(Set<Long> skuIds, SkuStatus skuStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM ProductSku s
        WHERE s.id IN :skuIds
        AND s.status <> :status
        ORDER BY s.id ASC
        """)
    List<ProductSku> findAllByIdForUpdate(@Param("skuIds") Set<Long> skuIds, @Param("status") SkuStatus status);

    List<ProductSku> findByProductIdAndStatusNot(Long productId, SkuStatus status);

    Optional<ProductSku> findByIdAndStatusNot(Long skuId, SkuStatus status);

    @EntityGraph(attributePaths = "product")
    Optional<ProductSku> findByIdAndProduct_IdAndStatusNot(Long changeSkuId, Long productId, SkuStatus status);

    boolean existsByProductIdAndId(Long productId, Long skuId);

    @Modifying
    @Query("""
        UPDATE ProductSku s
        SET s.reservedQuantity = s.reservedQuantity - :confirmQuantity,
            s.stockQuantity = s.stockQuantity - :confirmQuantity
        WHERE s.id = :skuId
          AND s.reservedQuantity >= :confirmQuantity
          AND s.stockQuantity >= :confirmQuantity
        """)
    int confirmStock(@Param("skuId") Long skuId, @Param("confirmQuantity") Integer confirmQuantity);

    @Modifying
    @Query("UPDATE ProductSku s SET s.stockQuantity = s.stockQuantity + :restoreQuantity WHERE s.id = :skuId")
    void restoreStock(@Param("skuId") Long skuId, @Param("restoreQuantity") Integer restoreQuantity);
}
