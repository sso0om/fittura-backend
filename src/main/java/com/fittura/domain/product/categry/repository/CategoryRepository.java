package com.fittura.domain.product.categry.repository;

import com.fittura.domain.product.categry.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findFirstByOrderByIdDesc();
}
