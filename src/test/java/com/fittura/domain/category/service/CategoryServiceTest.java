package com.fittura.domain.category.service;

import com.fittura.domain.category.constant.CategoryStatus;
import com.fittura.domain.category.dto.request.CategoryCreateReqDto;
import com.fittura.domain.category.dto.request.CategoryUpdateReqDto;
import com.fittura.domain.category.dto.response.CategoryResDto;
import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.error.CategoryErrorCode;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;


    // ========== 카테고리 단건 조회 ==========

    @Test
    @DisplayName("카테고리 단건 조회 성공")
    void getCategoryByIdSuccess() {
        // given
        Category category = CategoryFixture.root("루트", 1);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        // when
        CategoryResDto result = categoryService.getCategoryById(1L);

        // then
        assertThat(result.id()).isEqualTo(category.getId());
        assertThat(result.name()).isEqualTo(category.getName());
        assertThat(result.parentId()).isNull();
        assertThat(result.depth()).isEqualTo(category.getDepth());
        assertThat(result.sortOrder()).isEqualTo(category.getSortOrder());

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("카테고리 단건 조회 실패 - 존재하지 않는 카테고리")
    void getCategoryByIdNotFound() {
        // given
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategoryById(1L))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.NOT_FOUND_CATEGORY);

        verify(categoryRepository).findById(1L);
    }


    // ========== 카테고리 생성 ==========

    @Test
    @DisplayName("루트 카테고리 생성 성공")
    void createRootCategorySuccess() {
        // given
        CategoryCreateReqDto reqDto = new CategoryCreateReqDto("루트", null, 1);

        // when
        CategoryResDto result = categoryService.createCategory(reqDto);

        // then
        assertThat(result.name()).isEqualTo("루트");
        assertThat(result.parentId()).isNull();
        assertThat(result.depth()).isEqualTo(0);
        assertThat(result.sortOrder()).isEqualTo(1);

        verify(categoryRepository).save(argThat(category ->
            category.getName().equals("루트") &&
                category.getDepth() == 0 &&
                category.getParent() == null
        ));
    }

    @Test
    @DisplayName("자식 카테고리 생성 성공")
    void createChildCategorySuccess() {
        // given
        Category parent = CategoryFixture.root("부모", 1);
        CategoryCreateReqDto reqDto = new CategoryCreateReqDto("자식", 1L, 2);

        given(categoryRepository.findById(1L)).willReturn(Optional.of(parent));

        // when
        CategoryResDto result = categoryService.createCategory(reqDto);

        // then
        assertThat(result.name()).isEqualTo("자식");
        assertThat(result.parentId()).isEqualTo(parent.getId());
        assertThat(result.depth()).isEqualTo(parent.getDepth() + 1);
        assertThat(result.sortOrder()).isEqualTo(2);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("자식 카테고리 생성 실패 - 부모 카테고리 없음")
    void createChildCategoryFailWhenParentNotFound() {
        // given
        CategoryCreateReqDto reqDto = new CategoryCreateReqDto("자식", 1L, 2);
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.NOT_FOUND_PARENT_CATEGORY);

        verify(categoryRepository).findById(1L);
    }


    // ========== 카테고리 단건 수정 ==========

    @Test
    @DisplayName("카테고리 수정 성공 - 기본 정보 수정")
    void updateCategoryInfoSuccess() {
        // given
        Category category = CategoryFixture.root("기존", 1);
        CategoryUpdateReqDto reqDto = new CategoryUpdateReqDto("변경", null, 2);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        // when
        categoryService.updateCategory(1L, reqDto);

        // then
        assertThat(category.getName()).isEqualTo("변경");
        assertThat(category.getParent()).isNull();
        assertThat(category.getDepth()).isEqualTo(0);
        assertThat(category.getSortOrder()).isEqualTo(2);
        assertThat(category.getStatus()).isEqualTo(CategoryStatus.DISABLED);

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("카테고리 수정 성공 - 부모 변경")
    void updateCategoryParentSuccess() {
        // given
        Category root1 = CategoryFixture.root("루트1", 1);
        Category root2 = CategoryFixture.root("루트2", 2);
        Category category = CategoryFixture.child("자식", 1, root1);
        CategoryUpdateReqDto reqDto = new CategoryUpdateReqDto("자식변경", 2L, 3);

        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(categoryRepository.findById(2L)).willReturn(Optional.of(root2));

        // when
        categoryService.updateCategory(1L, reqDto);

        // then
        assertThat(category.getName()).isEqualTo("자식변경");
        assertThat(category.getParent()).isEqualTo(root2);
        assertThat(category.getDepth()).isEqualTo(root2.getDepth() + 1);
        assertThat(category.getSortOrder()).isEqualTo(3);
        assertThat(category.getStatus()).isEqualTo(CategoryStatus.DISABLED);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).findById(2L);
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않는 카테고리")
    void updateCategoryFailWhenCategoryNotFound() {
        // given
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());
        CategoryUpdateReqDto reqDto = new CategoryUpdateReqDto("변경", null, 2);

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.NOT_FOUND_CATEGORY);

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않는 부모 카테고리")
    void updateCategoryFailWhenParentNotFound() {
        // given
        Category category = CategoryFixture.root("기존", 1);
        CategoryUpdateReqDto reqDto = new CategoryUpdateReqDto("변경", 2L, 2);

        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(categoryRepository.findById(2L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.NOT_FOUND_PARENT_CATEGORY);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).findById(2L);
    }


    // ========== 카테고리 상태 변경 ==========
    @Test
    @DisplayName("카테고리 활성화 성공")
    void activeCategorySuccess() {
        // given
        Category category = CategoryFixture.root("상위 카테고리", 1);
        Category child = CategoryFixture.child("하위", 1, category);
        category.getChildren().add(child);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        // when
        categoryService.activeCategory(1L);

        // then
        assertThat(category.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
        assertThat(category.getChildren().getFirst().getStatus()).isEqualTo(CategoryStatus.DISABLED);

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("카테고리 비활성화 성공")
    void disableCategorySuccess() {
        // given
        Category category = CategoryFixture.rootActive();
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        // when
        categoryService.disableCategory(1L);

        // then
        assertThat(category.getStatus()).isEqualTo(CategoryStatus.DISABLED);

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategorySuccess() {
        // given
        Category category = CategoryFixture.rootActiveWithId(1L);

        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(categoryRepository.findDescendantIds(1L))
            .willReturn(List.of(1L, 2L, 3L));

        // when
        categoryService.deleteCategory(1L);

        // then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).findDescendantIds(1L);
        verify(categoryRepository)
            .bulkUpdateStatus(
                argThat(ids -> ids.containsAll(List.of(1L, 2L, 3L))),
                eq(CategoryStatus.ARCHIVED)
            );
    }
}