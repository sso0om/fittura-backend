package com.fittura.domain.product.sku.service;

import com.fittura.domain.product.product.dto.response.CompositionResDto;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuCreateReqDto;
import com.fittura.domain.product.sku.entity.ProductComposition;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.CompositionRepository;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
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


    // ========== ProductSku 생성 ==========

    @Test
    @DisplayName("SKU 생성 성공")
    void createSkusSuccess() {
        // given
        Product product = ProductFixture.component("Chair Leg", 5000L);
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


    // ========== Composition 생성 ==========

    @Test
    @DisplayName("Composition 생성 성공")
    void createCompositionsSuccess() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk", 100000L);
        Product componentProduct = ProductFixture.component("Chair Leg", 5000L);
        ProductSku componentSku = ProductSkuFixture.skuWithId(1L, componentProduct);

        given(productSkuRepository.findById(1L)).willReturn(Optional.of(componentSku));

        // when
        skuService.createCompositions(completeProduct, List.of(new CompositionCreateReqDto(1L, 4, 0)));

        // then
        verify(compositionRepository, times(1)).save(any(ProductComposition.class));
    }

    @Test
    @DisplayName("Composition 생성 실패 - SKU 없음")
    void createCompositionsFail_skuNotFound() {
        // given
        Product product = ProductFixture.complete("A Desk", 100000L);

        given(productSkuRepository.findById(99L)).willReturn(Optional.empty());

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
        Product product = ProductFixture.complete("A Desk", 100000L);
        ProductSku archivedSku = ProductSkuFixture.skuWithId(1L, ProductFixture.component("Chair Leg", 5000L));
        ReflectionTestUtils.setField(archivedSku, "status", SkuStatus.ARCHIVED);

        given(productSkuRepository.findById(1L)).willReturn(Optional.of(archivedSku));

        // when & then
        assertThatThrownBy(() -> skuService.createCompositions(product, List.of(new CompositionCreateReqDto(1L, 4, 0))))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.ARCHIVED_SKU);
    }

    @Test
    @DisplayName("Composition 생성 실패 - COMPLETE 상품의 SKU")
    void createCompositionsFail_skuIsComplete() {
        // given
        Product completeProduct = ProductFixture.complete("A Desk", 100000L);
        ProductSku completeSku = ProductSkuFixture.skuWithId(1L, completeProduct);

        given(productSkuRepository.findById(1L)).willReturn(Optional.of(completeSku));

        // when & then
        assertThatThrownBy(() -> skuService.createCompositions(completeProduct, List.of(new CompositionCreateReqDto(1L, 4, 0))))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(ProductErrorCode.CHILD_SKU_ONLY_COMPONENT);
    }


    // ========== 구성품 조회 ==========

    @Test
    @DisplayName("상품 구성 정보 조회 성공")
    void getProductCompositionsSuccess() {
        // given
        List<CompositionResDto> compositions = List.of(
            new CompositionResDto(1L, "의자 다리", 4, 0)
        );
        given(compositionRepository.findCompositionsByProductId(1L)).willReturn(compositions);

        // when
        List<CompositionResDto> result = skuService.getProductCompositions(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).childProductName()).isEqualTo("의자 다리");
        assertThat(result.get(0).quantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("상품 구성 정보 조회 성공 - 구성품 없음")
    void getProductCompositionsSuccess_empty() {
        // given
        given(compositionRepository.findCompositionsByProductId(99L)).willReturn(List.of());

        // when
        List<CompositionResDto> result = skuService.getProductCompositions(99L);

        // then
        assertThat(result).isEmpty();
    }
}