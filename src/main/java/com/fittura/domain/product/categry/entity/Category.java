package com.fittura.domain.product.categry.entity;

import com.fittura.domain.product.categry.constant.CategoryStatus;
import com.fittura.global.error.CommonErrorCode;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "categories")
@NoArgsConstructor(access = PROTECTED)
public class Category extends BaseEntity {
    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("sortOrder asc")
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    private int depth;

    @Column(nullable = false)
    private int sortOrder;

    @Setter
    @Enumerated(EnumType.STRING)
    private CategoryStatus status;

    private Category(String name, int sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
        this.status = CategoryStatus.DISABLED;
    }

    public static Category createRoot(String name, int sortOrder) {
        Category category = new Category(name, sortOrder);
        category.setDepth(0);
        return category;
    }

    public static Category createChild(String name, int sortOrder, Category parent) {
        if (parent == null) {
            throw new ServiceException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        Category category = new Category(name, sortOrder);
        parent.addChild(category);
        return category;
    }


    // ===== 연관관계 편의 메서드 =====

    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
        child.setDepth(this.depth + 1);
    }


    // ===== 헬퍼 메서드 ====

    private void setParent(Category parent) {
        this.parent = parent;
    }

    private void setDepth(int depth) {
        this.depth = depth;
    }
}
