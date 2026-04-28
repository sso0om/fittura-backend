package com.fittura.domain.product.categry.repository;

import com.fittura.domain.product.categry.constant.CategoryStatus;
import com.fittura.domain.product.categry.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findFirstByOrderByIdDesc();

    @Query(value = """
        WITH RECURSIVE descendants AS (
            SELECT id FROM categories WHERE id = :parentId
            UNION ALL
            SELECT c.id FROM categories c
            INNER JOIN descendants d ON c.parent_id = d.id
        )
        SELECT id FROM descendants
        """, nativeQuery = true)
    List<Long> findDescendantIds(@Param("parentId") Long parentId);

    @Modifying
    @Query("UPDATE Category c SET c.status = :status WHERE c.id IN :ids")
    void bulkUpdateStatus(@Param("ids") List<Long> ids, @Param("status") CategoryStatus status);
}
