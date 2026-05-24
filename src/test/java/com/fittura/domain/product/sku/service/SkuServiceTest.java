package com.fittura.domain.product.sku.service;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.ProductCompositionRepository;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SkuServiceTest {

    @Mock
    private ProductSkuRepository productSkuRepository;

    @Mock
    private ProductCompositionRepository compositionRepository;

    @InjectMocks
    private SkuService skuService;


    // ========== Composition 생성 ==========

    @Test
    @DisplayName("Composition 생성 실패 - SKU 없음")
    void createCompositionsFail_skuNotFound() {
        // given
        Product product = ProductFixture.complete("A Desk", 100000L);

        given(productSkuRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> skuService.createCompositions(product, List.of(new CompositionCreateReqDto(99L, 4, 0))))
            .isInstanceOf(ServiceException.class);
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
            .isInstanceOf(ServiceException.class);
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
            .isInstanceOf(ServiceException.class);
    }
}