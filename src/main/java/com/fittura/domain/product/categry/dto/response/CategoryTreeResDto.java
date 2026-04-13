package com.fittura.domain.product.categry.dto.response;

import com.fittura.domain.product.categry.entity.Category;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record CategoryTreeResDto(
    Long id,
    String name,
    Long parentId,
    int depth,
    int sortOrder,
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
