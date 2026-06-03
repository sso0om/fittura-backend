package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, ProductRepositoryCustom {
    Optional<Product> findByIdAndStatusNot(Long id, ProductStatus status);
}
