package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    List<ProductAttribute> findByProductId(Long productId);

    @Modifying
    @Query("DELETE ProductAttribute a WHERE a.product.id = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);
}
