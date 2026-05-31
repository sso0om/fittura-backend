package com.fittura.domain.product.product.service;

import com.fittura.domain.category.constant.CategoryStatus;
import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.error.CategoryErrorCode;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.product.product.constant.AttributeKey;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.dto.request.AttributeCreateReqDto;
import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.dto.response.ProductWithSkuResDto;
import com.fittura.domain.product.product.dto.response.ProductWithAllResDto;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuCreateReqDto;
import com.fittura.domain.product.sku.dto.response.SkuResDto;
import com.fittura.domain.product.sku.dto.response.SkuWithStockResDto;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    // ========== 사용자 상품 조회 ==========

    @Test
    @DisplayName("사용자 상품 조회 성공")
    void getProductWithSkuSuccess() {
        // given
        List<SkuResDto> skus = List.of(
            new SkuResDto(1L, 90000L, SkuStatus.ACTIVE, null, null)
        );
        ProductWithSkuResDto productWithSkuResDto = new ProductWithSkuResDto(
            1L, "A Desk", null, ProductType.COMPONENT, ProductStatus.ACTIVE,
            50000L, 10.0, 100.0, 75.0, 50.0, skus
        );

        given(productRepository.findWithSkuById(1L)).willReturn(Optional.of(productWithSkuResDto));

        // when
        ProductWithSkuResDto result = productService.getProductWithSku(1L);

        // then
        assertThat(result.name()).isEqualTo("A Desk");
        assertThat(result.skus()).hasSize(1);
    }

    @Test
    @DisplayName("사용자 상품 조회 실패 - 상품 없음")
    void getProductWithSkuFail_notFound() {
        // given
        given(productRepository.findWithSkuById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProductWithSku(99L))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.NOT_FOUND_PRODUCT);
    }


    // ========== 관리자 상품 조회 ==========

    @Test
    @DisplayName("상품 조회 성공")
    void getProductWithSkuWithStockSuccess() {
        // given
        List<SkuWithStockResDto> skus = List.of(
            new SkuWithStockResDto(1L, 90000L, 50, 0, SkuStatus.ACTIVE, null, null)
        );
        ProductWithAllResDto productWithAllResDto = new ProductWithAllResDto(
            1L, "A Desk", null, ProductType.COMPLETE, ProductStatus.DISABLED,
            100000L, 10.0, 100.0, 75.0, 50.0, skus
        );

        given(productRepository.findWithAllById(1L)).willReturn(Optional.of(productWithAllResDto));

        // when
        ProductWithAllResDto result = productService.getProductWithAll(1L);

        // then
        assertThat(result.name()).isEqualTo("A Desk");
        assertThat(result.skus()).hasSize(1);
    }

    @Test
    @DisplayName("상품 조회 실패 - 상품 없음")
    void getProductWithSkuWithStockFail_notFound() {
        // given
        given(productRepository.findWithAllById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProductWithAll(99L))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.NOT_FOUND_PRODUCT);
    }


    // ========== 상품 생성 ==========

    @Test
    @DisplayName("상품 생성 실패 - 카테고리 없음")
    void createFail_categoryNotFound() {
        // given
        ProductCreateReqDto reqDto = new ProductCreateReqDto(
            99L, "A Desk", null, ProductType.COMPLETE, 100000L,
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
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
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
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
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
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
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
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
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
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
        return new SkuCreateReqDto(10000L, 100, "White", "Wood");
    }

    private AttributeCreateReqDto attributeDto() {
        return new AttributeCreateReqDto(AttributeKey.SIZE_LABEL, "L");
    }

    private CompositionCreateReqDto compositionDto() {
        return new CompositionCreateReqDto(1L, 2, 0);
    }
}