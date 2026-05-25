package com.fittura.domain.product.sku.service;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuAttributeCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuCreateReqDto;
import com.fittura.domain.product.sku.entity.ProductComposition;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.entity.SkuAttribute;
import com.fittura.domain.product.sku.repository.ProductCompositionRepository;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuService {

    private final ProductSkuRepository productSkuRepository;
    private final ProductCompositionRepository compositionRepository;

    public void createSkus(Product product, List<SkuCreateReqDto> skuDtos) {
        for (SkuCreateReqDto skuDto : skuDtos) {
            ProductSku productSku = ProductSku.create(
                product,
                skuDto.price(),
                skuDto.stockQuantity(),
                skuDto.color(),
                skuDto.material(),
                skuDto.weight()
            );

            createAttributes(productSku, skuDto.attributes());
            productSkuRepository.save(productSku);
        }
    }

    private void createAttributes(ProductSku productSku, List<SkuAttributeCreateReqDto> attributesDtos) {
        for (SkuAttributeCreateReqDto skuAttributeDto : attributesDtos) {
            SkuAttribute.create(
                productSku,
                skuAttributeDto.attributeKey(),
                skuAttributeDto.attributeValue()
            );
        }
    }

    public void createCompositions(Product product, List<CompositionCreateReqDto> compositionDtos) {
        for (CompositionCreateReqDto compositionDto : compositionDtos) {
            ProductSku productSku = getProductSku(compositionDto.childSkuId());
            validateProductSkuForComposition(productSku);

            ProductComposition composition = ProductComposition.create(
                product,
                productSku,
                compositionDto.quantity(),
                compositionDto.sortOrder()
            );
            compositionRepository.save(composition);
        }
    }


    // ===== 유효성 검사 메서드 ====

    private void validateProductSkuForComposition(ProductSku productSku) {
        if (productSku.isArchived()) {
            throw new ServiceException(ProductErrorCode.ARCHIVED_SKU);
        }

        if (productSku.getProduct().isComplete()) {
            throw new ServiceException(ProductErrorCode.CHILD_SKU_ONLY_COMPONENT);
        }
    }


    // ===== 헬퍼 메서드 ====

    public ProductSku getProductSku(Long productSkuId) {
        return productSkuRepository.findById(productSkuId)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_SKU));
    }
}
