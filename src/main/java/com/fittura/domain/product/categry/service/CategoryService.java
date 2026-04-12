package com.fittura.domain.product.categry.service;

import com.fittura.domain.product.categry.dto.request.CategoryCreateReqDto;
import com.fittura.domain.product.categry.dto.response.CategoryDto;
import com.fittura.domain.product.categry.entity.Category;
import com.fittura.domain.product.categry.error.CategoryErrorCode;
import com.fittura.domain.product.categry.repository.CategoryRepository;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryDto createCategory(CategoryCreateReqDto reqDto) {
        Category category;

        if(reqDto.parentId() == null){
            category = Category.createRoot(reqDto.name(), reqDto.sortOrder());
        } else {
            Category parent = getParent(reqDto.parentId());
            category = Category.createChild(reqDto.name(), reqDto.sortOrder(), parent);
        }
        categoryRepository.save(category);

        return CategoryDto.from(category);
    }


    // ===== 헬퍼 메서드 ====

    private Category getParent(Long parentId) {
        return categoryRepository.findById(parentId)
            .orElseThrow(() -> new ServiceException(CategoryErrorCode.NOT_FOUND_CATEGORY));
    }
}
