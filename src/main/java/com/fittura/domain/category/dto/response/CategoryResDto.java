package com.fittura.domain.category.dto.response;

import com.fittura.domain.category.constant.CategoryStatus;
import com.fittura.domain.category.entity.Category;

public record CategoryResDto(
    Long id,
    String name,
    Long parentId,
    int depth,
    int sortOrder,
    CategoryStatus status
) {
    public static CategoryResDto from(Category category) {
        return new CategoryResDto(
            category.getId(),
            category.getName(),
            category.getParent() == null ? null : category.getParent().getId(),
            category.getDepth(),
            category.getSortOrder(),
            category.getStatus()
        );
    }
}
