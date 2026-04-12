package com.fittura.domain.product.categry.dto.response;

import com.fittura.domain.product.categry.entity.Category;

public record CategoryDto(
    Long id,
    String name,
    Long parentId,
    int depth,
    int sortOrder
) {
    public static CategoryDto from(Category category) {
        return new CategoryDto(
            category.getId(),
            category.getName(),
            category.getParent() == null ? null : category.getParent().getId(),
            category.getDepth(),
            category.getSortOrder()
        );
    }
}
