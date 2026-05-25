package com.fittura.domain.product.product.service;

import com.fittura.domain.category.constant.CategoryStatus;
import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.error.CategoryErrorCode;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuCreateReqDto;
import com.fittura.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;


    // ========== 상품 생성 ==========

    @Test
    @DisplayName("상품 생성 실패 - 카테고리 없음")
    void createFail_categoryNotFound() {
        // given
        ProductCreateReqDto reqDto = new ProductCreateReqDto(
            99L, "A Desk", null, ProductType.COMPLETE, 100000L,
            List.of(skuDto()),
            List.of(compositionDto())
        );

        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.createProduct(reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.NOT_FOUND_CATEGORY);
    }

    @Test
    @DisplayName("상품 생성 실패 - ARCHIVED 카테고리")
    void createFail_categoryArchived() {
        // given
        Category archived = CategoryFixture.rootActive();
        ReflectionTestUtils.setField(archived, "status", CategoryStatus.ARCHIVED);

        ProductCreateReqDto reqDto = new ProductCreateReqDto(
            1L, "A Desk", null, ProductType.COMPLETE, 100000L,
            List.of(skuDto()),
            List.of(compositionDto())
        );

        given(categoryRepository.findById(1L)).willReturn(Optional.of(archived));

        // when & then
        assertThatThrownBy(() -> productService.createProduct(reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.ARCHIVED_CATEGORY);
    }

    @Test
    @DisplayName("상품 생성 실패 - 리프 카테고리가 아님")
    void createFail_categoryNotLeaf() {
        // given
        Category parent = CategoryFixture.rootActive();
        CategoryFixture.childActive(parent); // parent에 자식 추가 → isLeaf() == false

        ProductCreateReqDto reqDto = new ProductCreateReqDto(
            1L, "A Desk", null, ProductType.COMPLETE, 100000L,
            List.of(skuDto()),
            List.of(compositionDto())
        );

        given(categoryRepository.findById(1L)).willReturn(Optional.of(parent));

        // when & then
        assertThatThrownBy(() -> productService.createProduct(reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.NOT_LEAF_CATEGORY);
    }

    @Test
    @DisplayName("완제품 생성 실패 - compositions 없음")
    void createCompleteFail_noCompositions() {
        // given
        ProductCreateReqDto reqDto = new ProductCreateReqDto(
            1L, "A Desk", null, ProductType.COMPLETE, 100000L,
            List.of(skuDto()),
            List.of()
        );

        given(categoryRepository.findById(1L)).willReturn(Optional.of(CategoryFixture.rootActive()));

        // when & then
        assertThatThrownBy(() -> productService.createProduct(reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.COMPLETE_HAVE_COMPOSITIONS);
    }

    @Test
    @DisplayName("단품 생성 실패 - compositions 있음")
    void createComponentFail_hasCompositions() {
        // given
        ProductCreateReqDto reqDto = new ProductCreateReqDto(
            1L, "A Desk", null, ProductType.COMPONENT, 100000L,
            List.of(skuDto()),
            List.of(compositionDto())  // compositions 있음
        );

        given(categoryRepository.findById(1L)).willReturn(Optional.of(CategoryFixture.rootActive()));

        // when & then
        assertThatThrownBy(() -> productService.createProduct(reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.COMPONENT_NOT_HAVE_COMPOSITION);
    }


    // ========== 핼퍼 메서드 ==========

    private SkuCreateReqDto skuDto() {
        return new SkuCreateReqDto(10000L, 100, "White", "Wood", 1.5, List.of());
    }

    private CompositionCreateReqDto compositionDto() {
        return new CompositionCreateReqDto(1L, 2, 0);
    }
}