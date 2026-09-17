package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorRepository extends JpaRepository<Color, Long> {
}
