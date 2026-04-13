package com.fittura.domain.product.categry.service;

import com.fittura.domain.product.categry.dto.request.CategoryCreateReqDto;
import com.fittura.domain.product.categry.dto.response.CategoryResDto;
import com.fittura.domain.product.categry.dto.response.CategoryTreeResDto;
import com.fittura.domain.product.categry.entity.Category;
import com.fittura.domain.product.categry.error.CategoryErrorCode;
import com.fittura.domain.product.categry.repository.CategoryRepository;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryTreeResDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        Map<Long, List<Category>> childrenMap = categories.stream()
            .filter(c -> c.getParent() != null)
            .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        return categories.stream()
            .filter(c -> c.getParent() == null)
            .sorted(Comparator.comparing(Category::getSortOrder))
            .map(root -> CategoryTreeResDto.from(root, childrenMap))
            .toList();
    }

    public CategoryResDto getCategoryById(Long id) {
        return CategoryResDto.from(getCategory(id));
    }

    public CategoryResDto createCategory(CategoryCreateReqDto reqDto) {
        Category category;

        if(reqDto.parentId() == null){
            category = Category.createRoot(reqDto.name(), reqDto.sortOrder());
        } else {
            Category parent = getParentCategory(reqDto.parentId());
            category = Category.createChild(reqDto.name(), reqDto.sortOrder(), parent);
        }
        categoryRepository.save(category);

        return CategoryResDto.from(category);
    }


    // ===== 헬퍼 메서드 ====

    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ServiceException(CategoryErrorCode.NOT_FOUND_CATEGORY));
    }

    public Category getParentCategory(Long parentId) {
        return categoryRepository.findById(parentId)
            .orElseThrow(() -> new ServiceException(CategoryErrorCode.NOT_FOUND_PARENT_CATEGORY));
    }
}
