package com.fittura.domain.category.dto.response;

import com.fittura.domain.category.constant.CategoryStatus;
import com.fittura.domain.category.entity.Category;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record CategoryTreeResDto(
    Long id,
    String name,
    Long parentId,
    int depth,
    int sortOrder,
    CategoryStatus status,
    List<CategoryTreeResDto> children
) {
    public static CategoryTreeResDto from(
        Category category,
        Map<Long, List<Category>> childrenMap
    ) {
        return new CategoryTreeResDto(
            category.getId(),
            category.getName(),
            category.getParent() == null ? null : category.getParent().getId(),
            category.getDepth(),
            category.getSortOrder(),
            category.getStatus(),
            fromChildren(category.getId(), childrenMap)
        );
    }

    private static List<CategoryTreeResDto> fromChildren(
        Long parentId,
        Map<Long, List<Category>> childrenMap
    ) {
        return childrenMap.getOrDefault(parentId, List.of()).stream()
            .sorted(Comparator.comparing(Category::getSortOrder))
            .map(child -> CategoryTreeResDto.from(child, childrenMap))
            .toList();
    }
}
