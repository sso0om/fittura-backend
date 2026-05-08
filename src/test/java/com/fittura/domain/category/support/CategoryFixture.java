package com.fittura.domain.category.support;

import com.fittura.domain.category.constant.CategoryStatus;
import com.fittura.domain.category.entity.Category;
import org.springframework.test.util.ReflectionTestUtils;

public class CategoryFixture {

    private CategoryFixture() {
    }

    public static Category root(String name, int sortOrder) {
        return Category.createRoot(name, sortOrder);
    }

    public static Category root(String name, int sortOrder, CategoryStatus status) {
        Category category = Category.createRoot(name, sortOrder);
        applyStatus(category, status);

        return category;
    }

    public static Category rootActiveWithId(Long id) {
        Category category = rootActive();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    public static Category child(String name, int sortOrder, Category parent) {
        return Category.createChild(name, sortOrder, parent);
    }

    public static Category child(String name, int sortOrder, Category parent, CategoryStatus status) {
        Category category = Category.createChild(name, sortOrder, parent);
        applyStatus(category, status);

        return category;
    }


    // ===== 기본값 세팅 메서드 =====

    public static Category rootActive() {
        return root("최상위 카테고리", 0, CategoryStatus.ACTIVE);
    }

    public static Category childActive(Category parent) {
        return child("하위 카테고리", 0, parent, CategoryStatus.ACTIVE);
    }


    // ===== 헬퍼 메서드 =====
    private static void applyStatus(Category category, CategoryStatus status) {
        switch (status) {
            case ACTIVE -> category.activate();
            case DISABLED -> category.disable();
            default -> throw new IllegalArgumentException("Unhandled status: " + status);
        }
    }
}