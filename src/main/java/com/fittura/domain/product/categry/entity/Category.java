package com.fittura.domain.product.categry.entity;

import com.fittura.domain.product.categry.constant.CategoryStatus;
import com.fittura.domain.product.categry.error.CategoryErrorCode;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        Objects.requireNonNull(parent, "parent must not be null");

        Category category = new Category(name, sortOrder);
        parent.addChild(category);
        return category;
    }

    public void update(String name, int sortOrder, CategoryStatus status) {
        this.name = name;
        this.sortOrder = sortOrder;
        this.status = status;
    }


    // ===== 연관관계 편의 메서드 =====

    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
        child.setDepth(this.depth + 1);
    }

    public void changeParent(Category newParent) {
        validateParent(newParent);

        detachFromParent();
        attachTo(newParent);

        int newDepth = newParent == null ? 0 : newParent.getDepth() + 1;
        updateDepthRecursively(newDepth);
    }

    private void detachFromParent() {
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = null;
    }

    private void attachTo(Category newParent) {
        if (newParent != null) {
            newParent.children.add(this);
        }
        this.parent = newParent;
    }

    private void updateDepthRecursively(int newDepth) {
        this.depth = newDepth;

        if (!this.children.isEmpty()) {
            for (Category child : this.children) {
                child.updateDepthRecursively(newDepth + 1);
            }
        }
    }


    // ===== 헬퍼 메서드 ====

    private void setParent(Category parent) {
        this.parent = parent;
    }

    private void setDepth(int depth) {
        this.depth = depth;
    }


    // ===== 검증 메서드 ====

    private void validateParent(Category newParent) {
        if (newParent == null) return;

        if (this.equals(newParent)) {
            throw new ServiceException(CategoryErrorCode.NOT_SELF_PARENT);
        }

        if (isDescendant(newParent)) {
            throw new ServiceException(CategoryErrorCode.NOT_DESCENDANT_PARENT);
        }
    }

    /**
     * 대상 카테고리가 현재 카테고리의 하위(자손)인지 검사
     * - 부모 변경 시 순환 참조 방지용
     *
     * @param newParent 신규 부모로 지정하려는 카테고리
     * @return newParent가 현재 카테고리의 자손이면 true
     */
    private boolean isDescendant(Category newParent) {
        Category current = newParent;

        while (current != null) {
            if (this.equals(current)) return true;
            current = current.getParent();
        }

        return false;
    }
}
