package com.fittura.domain.category.service;

import com.fittura.domain.category.constant.CategoryStatus;
import com.fittura.domain.category.dto.request.CategoryCreateReqDto;
import com.fittura.domain.category.dto.request.CategoryUpdateReqDto;
import com.fittura.domain.category.dto.response.CategoryResDto;
import com.fittura.domain.category.dto.response.CategoryTreeResDto;
import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.error.CategoryErrorCode;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryTreeResDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        return buildCategoryTree(categories);
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResDto> getActiveCategories() {
        List<Category> categories = categoryRepository.findAllVisible(CategoryStatus.ACTIVE);

        return buildCategoryTree(categories);
    }

    @Transactional(readOnly = true)
    public CategoryResDto getCategoryById(Long id) {
        return CategoryResDto.from(getCategory(id));
    }

    @Transactional
    public CategoryResDto createCategory(CategoryCreateReqDto reqDto) {
        Category category;

        if (reqDto.parentId() == null) {
            category = Category.createRoot(reqDto.name(), reqDto.sortOrder());
        } else {
            Category parent = getParentCategory(reqDto.parentId());
            category = Category.createChild(reqDto.name(), reqDto.sortOrder(), parent);
        }
        categoryRepository.save(category);

        return CategoryResDto.from(category);
    }

    @Transactional
    public void updateCategory(Long id, CategoryUpdateReqDto reqDto) {
        Category category = getCategory(id);

        category.update(reqDto.name(), reqDto.sortOrder());

        Category newParent = reqDto.parentId() == null
            ? null
            : getParentCategory(reqDto.parentId());

        if (!Objects.equals(category.getParent(), newParent)) {
            category.changeParent(newParent);
        }
    }

    @Transactional
    public CategoryResDto activeCategory(Long id) {
        Category category = getCategory(id);
        category.activate();

        return CategoryResDto.from(category);
    }

    @Transactional
    public CategoryResDto disableCategory(Long id) {
        Category category = getCategory(id);
        category.disable();

        return CategoryResDto.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategory(id);

        List<Long> descendantIds = categoryRepository.findDescendantIds(category.getId());

        if(!descendantIds.isEmpty()) {
            categoryRepository.bulkUpdateStatus(descendantIds, CategoryStatus.ARCHIVED);
        }
    }


    // ===== 헬퍼 메서드 ====

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ServiceException(CategoryErrorCode.NOT_FOUND_CATEGORY));
    }

    private Category getParentCategory(Long parentId) {
        return categoryRepository.findById(parentId)
            .orElseThrow(() -> new ServiceException(CategoryErrorCode.NOT_FOUND_PARENT_CATEGORY));
    }

    private static List<CategoryTreeResDto> buildCategoryTree(List<Category> categories) {
        Map<Long, List<Category>> childrenMap = categories.stream()
            .filter(c -> c.getParent() != null)
            .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        return categories.stream()
            .filter(c -> c.getParent() == null)
            .sorted(Comparator.comparing(Category::getSortOrder))
            .map(root -> CategoryTreeResDto.from(root, childrenMap))
            .toList();
    }
}
