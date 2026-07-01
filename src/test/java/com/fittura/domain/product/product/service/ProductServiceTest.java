package com.fittura.domain.product.product.service;

import com.fittura.domain.category.constant.CategoryStatus;
import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.error.CategoryErrorCode;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.product.product.constant.AttributeKey;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.dto.request.*;
import com.fittura.domain.product.product.dto.response.*;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.entity.ProductAttribute;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.repository.ProductAttributeRepository;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.product.support.ProductAttributeFixture;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuUpdateReqDto;
import com.fittura.domain.product.sku.dto.response.SkuResDto;
import com.fittura.domain.product.sku.dto.response.SkuWithStockResDto;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import com.fittura.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductAttributeRepository attributeRepository;

    @InjectMocks
    private ProductService productService;

    // ========== 상품 목록 조회 ==========

    @Test
    @DisplayName("상품 목록 조회 성공")
    void getProductsSuccess() {
        // given
        ProductSearchCondition condition = new ProductSearchCondition(List.of(ProductStatus.ACTIVE, ProductStatus.DISCONTINUED), null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductResDto> content = List.of(
            new ProductResDto(1L, "A Desk", 50000L, ProductStatus.ACTIVE, ProductType.COMPONENT, null, false),
            new ProductResDto(2L, "A Chair", 30000L, ProductStatus.DISCONTINUED, ProductType.COMPONENT, null, false)
        );
        Page<ProductResDto> page = new PageImpl<>(content, pageable, 2);

        given(productRepository.findProducts(condition, pageable)).willReturn(page);

        // when
        Page<ProductResDto> result = productService.getProducts(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("A Desk");
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - 결과 없음")
    void getProductsSuccess_empty() {
        // given
        ProductSearchCondition condition = new ProductSearchCondition(List.of(ProductStatus.ACTIVE), null, "없는상품", null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResDto> emptyPage = Page.empty(pageable);

        given(productRepository.findProducts(condition, pageable)).willReturn(emptyPage);

        // when
        Page<ProductResDto> result = productService.getProducts(condition, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - colors, materials 필터")
    void getProductsSuccess_withColorsAndMaterials() {
        // given
        ProductSearchCondition condition = new ProductSearchCondition(
            List.of(ProductStatus.ACTIVE, ProductStatus.DISCONTINUED),
            null, null,
            List.of("White"), List.of("Wood")
        );
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductResDto> content = List.of(
            new ProductResDto(1L, "Wood Desk", 50000L, ProductStatus.ACTIVE, ProductType.COMPONENT, null, false)
        );
        Page<ProductResDto> page = new PageImpl<>(content, pageable, 1);

        given(productRepository.findProducts(condition, pageable)).willReturn(page);

        // when
        Page<ProductResDto> result = productService.getProducts(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Wood Desk");
    }


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
            50000L, 10.0, 100.0, 75.0, 50.0, false, skus
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
    void getProductWithAllSuccess() {
        // given
        List<SkuWithStockResDto> skus = List.of(
            new SkuWithStockResDto(1L, 90000L, 50, 0, SkuStatus.ACTIVE, null, null)
        );
        List<ProductAttributeResDto> attributes = List.of(
            new ProductAttributeResDto(1L, AttributeKey.SIZE_LABEL, "XL")
        );
        List<CompositionResDto> compositions = List.of(
            new CompositionResDto(1L, "의자 다리", 4, 0)
        );
        ProductWithAllResDto productWithAllResDto = new ProductWithAllResDto(
            1L, "A Desk", null, ProductType.COMPLETE, ProductStatus.DISABLED,
            100000L, 10.0, 100.0, 75.0, 50.0, false, skus, attributes, compositions
        );

        given(productRepository.findWithAllById(1L)).willReturn(Optional.of(productWithAllResDto));

        // when
        ProductWithAllResDto result = productService.getProductWithAll(1L);

        // then
        assertThat(result.name()).isEqualTo("A Desk");
        assertThat(result.skus()).hasSize(1);
        assertThat(result.attributes()).hasSize(1);
        assertThat(result.compositions()).hasSize(1);
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
            99L, "A Desk", null, ProductType.COMPLETE,
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
            List.of(compositionDto())
        );

        givenCategoryNotFound(99L);

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
            1L, "A Desk", null, ProductType.COMPLETE,
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
            List.of(compositionDto())
        );

        givenCategoryFound(1L, archived);

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
            1L, "A Desk", null, ProductType.COMPLETE,
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
            List.of(compositionDto())
        );

        givenCategoryFound(1L, parent);

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
            1L, "A Desk", null, ProductType.COMPLETE,
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
            List.of()
        );

        givenCategoryFound(1L, CategoryFixture.rootActive());

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
            1L, "A Desk", null, ProductType.COMPONENT,
            40.5, 150.0, 100.0, 50.0,
            List.of(skuDto()),
            List.of(attributeDto()),
            List.of(compositionDto())  // compositions 있음
        );

        givenCategoryFound(1L, CategoryFixture.rootActive());

        // when & then
        assertThatThrownBy(() -> productService.createProduct(reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.COMPONENT_NOT_HAVE_COMPOSITION);
    }


    // ========== 상품 수정 ==========

    @Test
    @DisplayName("상품 수정 성공")
    void updateProductSuccess() {
        // given
        Category newCategory = CategoryFixture.rootActive();
        Product product = ProductFixture.component("Old Name");

        ProductUpdateReqDto reqDto = new ProductUpdateReqDto(
            1L, "New Name", "새 설명",
            20.0, 200.0, 120.0, 60.0,
            List.of(skuUpdateDto(null)), List.of(), List.of()
        );

        givenCategoryFound(1L, newCategory);

        // when
        productService.updateProduct(product, reqDto);

        // then
        assertThat(product.getName()).isEqualTo("New Name");
        assertThat(product.getCategory()).isEqualTo(newCategory);
    }

    @Test
    @DisplayName("상품 수정 실패 - 카테고리 없음")
    void updateProductFail_categoryNotFound() {
        // given
        Product product = ProductFixture.component("Old Name");
        ProductUpdateReqDto reqDto = new ProductUpdateReqDto(
            99L, "New Name", null,
            20.0, 200.0, 120.0, 60.0,
            List.of(skuUpdateDto(null)), List.of(), List.of()
        );

        givenCategoryNotFound(99L);

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(product, reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.NOT_FOUND_CATEGORY);
    }

    @Test
    @DisplayName("상품 수정 실패 - ARCHIVED 카테고리")
    void updateProductFail_categoryArchived() {
        // given
        Category archived = CategoryFixture.rootActive();
        ReflectionTestUtils.setField(archived, "status", CategoryStatus.ARCHIVED);

        Product product = ProductFixture.component("Old Name");
        ProductUpdateReqDto reqDto = new ProductUpdateReqDto(
            1L, "New Name", null,
            20.0, 200.0, 120.0, 60.0,
            List.of(skuUpdateDto(null)), List.of(), List.of()
        );

        givenCategoryFound(1L, archived);

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(product, reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.ARCHIVED_CATEGORY);
    }

    @Test
    @DisplayName("상품 수정 실패 - 리프 카테고리가 아님")
    void updateProductFail_categoryNotLeaf() {
        // given
        Category parent = CategoryFixture.rootActive();
        CategoryFixture.childActive(parent);

        Product product = ProductFixture.component("Old Name");
        ProductUpdateReqDto reqDto = new ProductUpdateReqDto(
            1L, "New Name", null,
            20.0, 200.0, 120.0, 60.0,
            List.of(skuUpdateDto(null)), List.of(), List.of()
        );

        givenCategoryFound(1L, parent);

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(product, reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(CategoryErrorCode.NOT_LEAF_CATEGORY);
    }


    // ========== basePrice 동기화 ==========

    @Test
    @DisplayName("basePrice 동기화 성공")
    void syncBasePriceSuccess() {
        // given
        Product product = ProductFixture.component("책상");
        ProductSkuFixture.sku(product, 30000L, 10);

        // when
        productService.syncBasePrice(product);

        // then
        assertThat(product.getBasePrice()).isEqualTo(30000L);
    }


    // ========== 상품 비활성화 ==========

    @Test
    @DisplayName("상품 비활성화 성공 - DISABLED 상태로 변경")
    void disableProductSuccess() {
        // given
        Product product = ProductFixture.component("A Desk");
        ReflectionTestUtils.setField(product, "status", ProductStatus.ACTIVE);
        given(productRepository.findByIdAndStatusNot(1L, ProductStatus.ARCHIVED))
            .willReturn(Optional.of(product));

        // when
        productService.disableProduct(1L);

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DISABLED);
    }


    // ========== 상품 단종 ==========

    @Test
    @DisplayName("상품 단종 성공 - DISCONTINUED 상태로 변경")
    void discontinueProductSuccess() {
        // given
        Product product = ProductFixture.component("A Desk");
        given(productRepository.findByIdAndStatusNot(1L, ProductStatus.ARCHIVED))
            .willReturn(Optional.of(product));

        // when
        productService.discontinueProduct(1L);

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
    }


    // ========== 상품 삭제 ==========

    @Test
    @DisplayName("상품 삭제 성공 - ARCHIVED 상태로 변경")
    void deleteProductSuccess() {
        // given
        Product product = ProductFixture.component("Chair Leg");

        // when
        productService.deleteProduct(product);

        // then
        assertThat(product.isArchived()).isTrue();
    }

    @Test
    @DisplayName("속성 삭제 성공 - 상품 ID로 전체 삭제")
    void deleteProductAttributesSuccess() {
        // given
        Product product = ProductFixture.componentWithId(1L, "A Desk");

        // when
        productService.deleteProductAttributes(product);

        // then
        verify(attributeRepository).deleteAllByProductId(product.getId());
    }


    // ========== 속성 수정 ==========

    @Test
    @DisplayName("속성 수정 성공 - 기존 속성 값 변경")
    void updateProductAttributeSuccess_updateExisting() {
        // given
        Product product = ProductFixture.component("A Desk");
        ProductAttribute existing = ProductAttributeFixture.productAttribute(product, AttributeKey.SIZE_LABEL, "M");
        ReflectionTestUtils.setField(existing, "id", 1L);

        given(attributeRepository.findByProductId(product.getId())).willReturn(List.of(existing));

        List<AttributeUpdateReqDto> reqDto = List.of(
            new AttributeUpdateReqDto(1L, AttributeKey.SIZE_LABEL, "XL")
        );

        // when
        productService.updateProductAttribute(product, reqDto);

        // then
        assertThat(existing.getAttributeValue()).isEqualTo("XL");
    }

    @Test
    @DisplayName("속성 수정 성공 - 새 속성 추가")
    void updateProductAttributeSuccess_addNew() {
        // given
        Product product = ProductFixture.component("A Desk");

        given(attributeRepository.findByProductId(product.getId())).willReturn(List.of());

        List<AttributeUpdateReqDto> reqDto = List.of(
            new AttributeUpdateReqDto(null, AttributeKey.SIZE_LABEL, "L")
        );

        // when
        productService.updateProductAttribute(product, reqDto);

        // then
        assertThat(product.getAttributes()).hasSize(1);
        assertThat(product.getAttributes().get(0).getAttributeValue()).isEqualTo("L");
    }

    @Test
    @DisplayName("속성 수정 성공 - 기존 속성 삭제")
    void updateProductAttributeSuccess_deleteRemoved() {
        // given
        Product product = ProductFixture.component("A Desk");
        ProductAttribute toDelete = ProductAttributeFixture.productAttribute(product, AttributeKey.SIZE_LABEL, "M");

        given(attributeRepository.findByProductId(product.getId())).willReturn(List.of(toDelete));

        // when - 빈 리스트를 전달하면 기존 속성은 삭제
        productService.updateProductAttribute(product, List.of());

        // then
        verify(attributeRepository).delete(toDelete);
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

    private SkuUpdateReqDto skuUpdateDto(Long id) {
        return new SkuUpdateReqDto(id, 10000L, 100, "White", "Wood");
    }

    private void givenCategoryNotFound(Long categoryId) {
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());
    }

    private void givenCategoryFound(Long categoryId, Category category) {
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
    }
}