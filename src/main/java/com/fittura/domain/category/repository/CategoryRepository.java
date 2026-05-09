package com.fittura.domain.category.repository;

import com.fittura.domain.category.constant.CategoryStatus;
import com.fittura.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findFirstByOrderByIdDesc();

    @Query("""
        SELECT c FROM Category c
        LEFT JOIN c.parent p
        WHERE c.status = :status
        AND (p IS NULL OR p.status = :status)
        """)
    // ACTIVE인 루트이거나 부모와 자신 모두 ACTIVE인 카테고리만 조회
    List<Category> findAllVisible(@Param("status") CategoryStatus status);

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

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Category c SET c.status = :status WHERE c.id IN :ids")
    void bulkUpdateStatus(@Param("ids") List<Long> ids, @Param("status") CategoryStatus status);
}
