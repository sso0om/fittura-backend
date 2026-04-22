package com.fittura.domain.product.categry.support;

import com.fittura.domain.product.categry.constant.CategoryStatus;
import com.fittura.domain.product.categry.entity.Category;

public class CategoryFixture {

    private CategoryFixture() {
    }

    public static Category root(String name, int sortOrder) {
        return Category.createRoot(name, sortOrder);
    }

    public static Category root(String name, int sortOrder, CategoryStatus status) {
        Category category = Category.createRoot(name, sortOrder);
        category.activate();

        return category;
    }

    public static Category child(String name, int sortOrder, Category parent) {
        return Category.createChild(name, sortOrder, parent);
    }

    public static Category child(String name, int sortOrder, Category parent, CategoryStatus status) {
        Category category = Category.createChild(name, sortOrder, parent);
        category.activate();

        return category;
    }


    // ===== 기본값 세팅 메서드 =====

    public static Category rootActive() {
        return root("최상위 카테고리", 0, CategoryStatus.ACTIVE);
    }
}