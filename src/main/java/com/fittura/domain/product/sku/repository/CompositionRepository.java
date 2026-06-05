package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.entity.ProductComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompositionRepository extends JpaRepository<ProductComposition, Long>, CompositionRepositoryCustom {
    List<ProductComposition> findByParentProductId(Long productId);

    @Modifying
    @Query("DELETE FROM ProductComposition c WHERE c.parentProduct.id = :parentProductId")
    void deleteAllByParentProductId(@Param("parentProductId") Long parentProductId);
}
