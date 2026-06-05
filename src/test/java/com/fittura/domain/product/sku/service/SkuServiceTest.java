package com.fittura.domain.product.sku.service;

import com.fittura.domain.product.product.dto.response.CompositionResDto;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.dto.request.CompositionUpdateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuUpdateReqDto;
import com.fittura.domain.product.sku.entity.ProductComposition;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.CompositionRepository;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.domain.product.sku.support.ProductCompositionFixture;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SkuServiceTest {

    @Mock
    private ProductSkuRepository productSkuRepository;

    @Mock
    private CompositionRepository compositionRepository;

    @InjectMocks
    private SkuService skuService;


    // ========== SKU 생성 ==========

    @Test
    @DisplayName("SKU 생성 성공")
    void createSkusSuccess() {
        // given
        Product product = ProductFixture.component("Chair Leg");
        List<SkuCreateReqDto> skuDtos = List.of(
            new SkuCreateReqDto(4500L, 100, "White", "Wood"),
            new SkuCreateReqDto(4800L, 50, "Black", "Metal")
        );

        // when
        skuService.createSkus(product, skuDtos);

        // then
        verify(productSkuRepository, times(2)).save(any(ProductSku.class));
        assertThat(product.getProductSkus()).hasSize(2);
    }


    // ========== SKU 수정 ==========

    @Test
    @DisplayName("SKU 수정 성공 - 기존 SKU 업데이트")
    void updateSkuSuccess_updateExisting() {
        // given
        Product product = ProductFixture.component("A Desk");
        ProductSku existing = ProductSkuFixture.skuWithId(1L, product);

        givenSkus(product.getId(), List.of(existing));

        List<SkuUpdateReqDto> reqDto = List.of(
            new SkuUpdateReqDto(1L, 9000L, 80, "Black", "Metal")
        );

        // when
        skuService.updateSku(product, reqDto);

        // then
        assertThat(existing.getPrice()).isEqualTo(9000L);
        assertThat(existing.getStockQuantity()).isEqualTo(80);
        assertThat(existing.getColor()).isEqualTo("Black");
        assertThat(existing.getMaterial()).isEqualTo("Metal");
    }

    @Test
    @DisplayName("SKU 수정 성공 - 새 SKU 추가")
    void updateSkuSuccess_addNew() {
        // given
        Product product = ProductFixture.component("A Desk");
        ProductSku existing = ProductSkuFixture.skuWithId(1L, product);

        givenSkus(product.getId(), List.of(existing));

        List<SkuUpdateReqDto> reqDto = List.of(
            new SkuUpdateReqDto(1L, 20000L, 50, "White", "Wood"),
            new SkuUpdateReqDto(null, 15000L, 30, "Black", "Metal")
        );

        // when
        skuService.updateSku(product, reqDto);

        // then
        verify(productSkuRepository, times(1)).save(any(ProductSku.class));
    }

    @Test
    @DisplayName("SKU 수정 성공 - 기존 SKU 삭제")
    void updateSkuSuccess_deleteRemoved() {
        // given
        Product product = ProductFixture.componentWithId(1L, "A Desk");

        ProductSku toDelete = ProductSkuFixture.skuWithId(1L, product);
        ProductSku toKeep = ProductSkuFixture.skuWithId(2L, product);

        givenSkus(product.getId(), List.of(toDelete, toKeep));

        List<SkuUpdateReqDto> reqDto = List.of(
            new SkuUpdateReqDto(2L, 20000L, 50, "White", "Wood")
        );

        // when
        skuService.updateSku(product, reqDto);

        // then
        assertThat(toDelete.isArchived()).isTrue();
    }


    // ========== SKU 삭제 ==========

    @Test
    @DisplayName("SKU 삭제 성공 - 모든 SKU ARCHIVED 처리")
    void deleteSkusSuccess() {
        // given
        Product product = ProductFixture.componentWithId(1L, "Chair Leg");
        ProductSku sku1 = ProductSkuFixture.skuWithId(1L, product);
        ProductSku sku2 = ProductSkuFixture.skuWithId(2L, product);

        givenSkus(product.getId(), List.of(sku1, sku2));

        // when
        skuService.deleteSkus(product);

        // then
        assertThat(sku1.isArchived()).isTrue();
        assertThat(sku2.isArchived()).isTrue();
    }


    // ========== 구성품 조회 ==========

    @Test
    @DisplayName("상품 구성 조회 성공")
    void getProductCompositionDtosSuccess() {
        // given
        List<CompositionResDto> compositions = List.of(
            new CompositionResDto(1L, "의자 다리", 4, 0)
        );
        given(compositionRepository.findCompositionDtosByProductId(1L)).willReturn(compositions);

        // when
        List<CompositionResDto> result = skuService.getProductCompositionDtos(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).childProductName()).isEqualTo("의자 다리");
        assertThat(result.get(0).quantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("상품 구성 조회 성공 - 구성품 없음")
    void getProductCompositionDtosSuccess_empty() {
        // given
        given(compositionRepository.findCompositionDtosByProductId(99L)).willReturn(List.of());

        // when
        List<CompositionResDto> result = skuService.getProductCompositionDtos(99L);

        // then
        assertThat(result).isEmpty();
    }


    // ========== 구성품 생성 ==========

    @Test
    @DisplayName("Composition 생성 성공")
    void createCompositionsSuccess() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk");
        Product componentProduct = ProductFixture.component("Chair Leg");
        ProductSku componentSku = ProductSkuFixture.skuWithId(1L, componentProduct);

        givenSkuFound(1L, componentSku);

        // when
        skuService.createCompositions(completeProduct, List.of(new CompositionCreateReqDto(1L, 4, 0)));

        // then
        verify(compositionRepository, times(1)).save(any(ProductComposition.class));
    }

    @Test
    @DisplayName("Composition 생성 실패 - SKU 없음")
    void createCompositionsFail_skuNotFound() {
        // given
        Product product = ProductFixture.complete("A Desk");

        givenSkuNotFound(99L);

        // when & then
        assertThatThrownBy(() -> skuService.createCompositions(product, List.of(new CompositionCreateReqDto(99L, 4, 0))))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.NOT_FOUND_SKU);
    }

    @Test
    @DisplayName("Composition 생성 실패 - ARCHIVED SKU")
    void createCompositionsFail_skuArchived() {
        // given
        Product product = ProductFixture.complete("A Desk");
        ProductSku archivedSku = ProductSkuFixture.skuWithId(1L, ProductFixture.component("Chair Leg"));
        ReflectionTestUtils.setField(archivedSku, "status", SkuStatus.ARCHIVED);

        givenSkuNotFound(1L);

        // when & then
        assertThatThrownBy(() -> skuService.createCompositions(product, List.of(new CompositionCreateReqDto(1L, 4, 0))))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.NOT_FOUND_SKU);
    }

    @Test
    @DisplayName("Composition 생성 실패 - COMPLETE 상품의 SKU")
    void createCompositionsFail_skuIsComplete() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk");
        ProductSku completeSku = ProductSkuFixture.skuWithId(1L, completeProduct);

        givenSkuFound(1L, completeSku);

        // when & then
        assertThatThrownBy(() -> skuService.createCompositions(completeProduct, List.of(new CompositionCreateReqDto(1L, 4, 0))))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.CHILD_SKU_ONLY_COMPONENT);
    }

    
    // ========== 구성품 수정 ==========

    @Test
    @DisplayName("상품 구성 수정 성공 - 기존 Composition 업데이트")
    void updateCompositionsSuccess_updateExisting() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk");
        Product componentProduct = ProductFixture.component("Chair Leg");
        ProductSku childSku = ProductSkuFixture.skuWithId(1L, componentProduct);
        ProductComposition existing = ProductCompositionFixture.composition(completeProduct, childSku, 2, 0);

        givenCompositions(completeProduct.getId(), List.of(existing));

        List<CompositionUpdateReqDto> reqDto = List.of(
            new CompositionUpdateReqDto(null, 1L, 4, 1)
        );

        // when
        skuService.updateCompositions(completeProduct, reqDto);

        // then
        assertThat(existing.getQuantity()).isEqualTo(4);
        assertThat(existing.getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("상품 구성 수정 성공 - 새 Composition 추가")
    void updateCompositionsSuccess_addNew() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk");
        Product componentProduct = ProductFixture.component("Chair Leg");
        ProductSku existingChildSku = ProductSkuFixture.skuWithId(1L, componentProduct);
        ProductSku newChildSku = ProductSkuFixture.skuWithId(2L, componentProduct);
        ProductComposition existing = ProductCompositionFixture.composition(completeProduct, existingChildSku, 2, 0);

        givenCompositions(completeProduct.getId(), List.of(existing));
        givenSkuFound(2L, newChildSku);

        List<CompositionUpdateReqDto> reqDto = List.of(
            new CompositionUpdateReqDto(null, 1L, 2, 0),
            new CompositionUpdateReqDto(null, 2L, 1, 1)
        );

        // when
        skuService.updateCompositions(completeProduct, reqDto);

        // then
        verify(compositionRepository, times(1)).save(any(ProductComposition.class));
    }

    @Test
    @DisplayName("상품 구성 수정 성공 - 기존 Composition 삭제")
    void updateCompositionsSuccess_deleteRemoved() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk");
        Product componentProduct = ProductFixture.component("Chair Leg");
        ProductSku toDeleteSku = ProductSkuFixture.skuWithId(1L, componentProduct);
        ProductSku toKeepSku = ProductSkuFixture.skuWithId(2L, componentProduct);
        ProductComposition toDelete = ProductCompositionFixture.composition(completeProduct, toDeleteSku, 2, 0);
        ProductComposition toKeep = ProductCompositionFixture.composition(completeProduct, toKeepSku, 1, 1);

        givenCompositions(completeProduct.getId(), List.of(toDelete, toKeep));

        List<CompositionUpdateReqDto> reqDto = List.of(
            new CompositionUpdateReqDto(null, 2L, 1, 1)
        );

        // when
        skuService.updateCompositions(completeProduct, reqDto);

        // then
        verify(compositionRepository, times(1)).delete(toDelete);
    }

    @Test
    @DisplayName("상품 구성 수정 실패 - SKU 없음")
    void updateCompositionsFail_skuNotFound() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk");

        givenCompositions(completeProduct.getId(), List.of());
        givenSkuNotFound(99L);

        List<CompositionUpdateReqDto> reqDto = List.of(
            new CompositionUpdateReqDto(null, 99L, 2, 0)
        );

        // when & then
        assertThatThrownBy(() -> skuService.updateCompositions(completeProduct, reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.NOT_FOUND_SKU);
    }

    @Test
    @DisplayName("상품 구성 수정 실패 - ARCHIVED SKU")
    void updateCompositionsFail_skuArchived() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk");
        ProductSku archivedSku = ProductSkuFixture.skuWithId(1L, ProductFixture.component("Chair Leg"));
        ReflectionTestUtils.setField(archivedSku, "status", SkuStatus.ARCHIVED);

        givenCompositions(completeProduct.getId(), List.of());
        givenSkuNotFound(1L);

        List<CompositionUpdateReqDto> reqDto = List.of(
            new CompositionUpdateReqDto(null, 1L, 2, 0)
        );

        // when & then
        assertThatThrownBy(() -> skuService.updateCompositions(completeProduct, reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.NOT_FOUND_SKU);
    }

    @Test
    @DisplayName("상품 구성 수정 실패 - COMPLETE 상품의 SKU")
    void updateCompositionsFail_skuIsComplete() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk");
        ProductSku completeSku = ProductSkuFixture.skuWithId(1L, completeProduct);

        givenCompositions(completeProduct.getId(), List.of());
        givenSkuFound(1L, completeSku);

        List<CompositionUpdateReqDto> reqDto = List.of(
            new CompositionUpdateReqDto(null, 1L, 2, 0)
        );

        // when & then
        assertThatThrownBy(() -> skuService.updateCompositions(completeProduct, reqDto))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.CHILD_SKU_ONLY_COMPONENT);
    }


    // ========== 구성품 삭제 ==========

    @Test
    @DisplayName("구성품 삭제 성공 - 완제품 ID로 전체 삭제")
    void deleteCompositionsSuccess() {
        // given
        Product completeProduct = ProductFixture.componentWithId(1L, "A Desk");

        // when
        skuService.deleteCompositions(completeProduct);

        // then
        verify(compositionRepository).deleteAllByParentProductId(completeProduct.getId());
    }


    // ========== 유효성 검사 - SKU 삭제 ==========

    @Test
    @DisplayName("SKU 삭제 유효성 검사 성공 - 완제품은 참조 체크 없이 통과")
    void validateDeletableSku_completeProduct() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk");

        // when & then (no exception)
        skuService.validateDeletableSku(completeProduct);
    }

    @Test
    @DisplayName("SKU 삭제 유효성 검사 성공 - 단품이고 다른 완제품에서 미참조")
    void validateDeletableSku_component_notReferenced() {
        // given
        Product componentProduct = ProductFixture.componentWithId(1L, "Chair Leg");
        given(compositionRepository.isSkuReferencedByOther(componentProduct.getId())).willReturn(false);

        // when & then (no exception)
        skuService.validateDeletableSku(componentProduct);
    }

    @Test
    @DisplayName("SKU 삭제 유효성 검사 실패 - 단품 SKU가 다른 완제품의 구성품으로 참조됨")
    void validateDeletableSku_component_referenced() {
        // given
        Product componentProduct = ProductFixture.componentWithId(1L, "Chair Leg");
        given(compositionRepository.isSkuReferencedByOther(componentProduct.getId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> skuService.validateDeletableSku(componentProduct))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.PRODUCT_SKU_REFERENCED_BY_OTHER);
    }


    // ========== 헬퍼 메서드 ==========

    private void givenSkuNotFound(Long skuId) {
        given(productSkuRepository.findByIdAndStatusNot(skuId, SkuStatus.ARCHIVED))
            .willReturn(Optional.empty());
    }

    private void givenSkuFound(Long skuId, ProductSku sku) {
        given(productSkuRepository.findByIdAndStatusNot(skuId, SkuStatus.ARCHIVED))
            .willReturn(Optional.of(sku));
    }

    private void givenSkus(Long productId, List<ProductSku> skus) {
        given(productSkuRepository.findByProductIdAndStatusNot(productId, SkuStatus.ARCHIVED))
            .willReturn(skus);
    }

    private void givenCompositions(Long productId, List<ProductComposition> compositions) {
        given(compositionRepository.findByParentProductId(productId)).willReturn(compositions);
    }
}