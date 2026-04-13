package com.fittura.domain.product.categry.dto.response;

import com.fittura.domain.product.categry.entity.Category;

public record CategoryResDto(
    Long id,
    String name,
    Long parentId,
    int depth,
    int sortOrder
) {
    public static CategoryResDto from(Category category) {
        return new CategoryResDto(
            category.getId(),
            category.getName(),
            category.getParent() == null ? null : category.getParent().getId(),
            category.getDepth(),
            category.getSortOrder()
        );
    }
}
