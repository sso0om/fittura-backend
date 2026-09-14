package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material,Long> {
}
