package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {

}
