package com.fittura.domain.product.sku.service;

import com.fittura.domain.product.product.dto.response.CompositionResDto;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.dto.request.CompositionUpdateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuUpdateReqDto;
import com.fittura.domain.product.sku.entity.ProductComposition;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.CompositionRepository;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuService {

    private final ProductSkuRepository productSkuRepository;
    private final CompositionRepository compositionRepository;

    public void createSkus(Product product, List<SkuCreateReqDto> skuDtos) {
        for (SkuCreateReqDto skuDto : skuDtos) {

            ProductSku productSku = ProductSku.create(
                product,
                skuDto.price(),
                skuDto.stockQuantity(),
                skuDto.color(),
                skuDto.material()
            );
            productSkuRepository.save(productSku);
        }
    }

    public void updateSku(Product product, List<SkuUpdateReqDto> reqDto) {
        List<ProductSku> existing = getProductSkus(product.getId());

        Map<Long, ProductSku> existingMap = existing.stream()
            .collect(Collectors.toMap(ProductSku::getId, s -> s));

        Set<Long> incomingIds = reqDto.stream()
            .map(SkuUpdateReqDto::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // id가 null이 아니고 existingMap에 없을 때: 다른 상품 SKU 또는 존재하지 않는 id
        incomingIds.forEach(id -> {
            if (!existingMap.containsKey(id)) {
                throw new ServiceException(ProductErrorCode.NOT_FOUND_SKU);
            }
        });

        existing.stream()
            .filter(s -> !incomingIds.contains(s.getId()))
            .forEach(ProductSku::archive);

        for (SkuUpdateReqDto dto : reqDto) {
            if (dto.id() == null) {
                ProductSku newSku = ProductSku.create(
                    product,
                    dto.price(),
                    dto.stockQuantity(),
                    dto.color(),
                    dto.material()
                );
                productSkuRepository.save(newSku);
            } else {
                ProductSku sku = existingMap.get(dto.id());
                sku.update(dto.price(), dto.stockQuantity(), dto.color(), dto.material());
            }
        }
    }

    public void soldOutSku(Long productId, Long skuId) {
        validateSkuOwnedByProduct(productId, skuId);

        ProductSku sku = getProductSku(skuId);
        sku.soldOut();
    }

    public void deleteSkus(Product product) {
        for (ProductSku productSku : getProductSkus(product.getId())) {
            productSku.archive();
        }
    }


    // ========== 구성품 ==========

    public List<CompositionResDto> getProductCompositionDtos(Long productId) {
        return compositionRepository.findCompositionDtosByProductId(productId);
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

    public void updateCompositions(Product product, List<CompositionUpdateReqDto> reqDto) {
        List<ProductComposition> existing = getProductCompositions(product.getId());

        Map<Long, ProductComposition> existingMap = existing.stream()
            .collect(Collectors.toMap(c -> c.getChildSku().getId(), c -> c));

        Set<Long> incomingSkuIds = reqDto.stream()
            .map(CompositionUpdateReqDto::childSkuId)
            .collect(Collectors.toSet());

        existing.stream()
            .filter(c -> !incomingSkuIds.contains(c.getChildSku().getId()))
            .forEach(compositionRepository::delete);

        for (CompositionUpdateReqDto dto : reqDto) {
            ProductComposition composition = existingMap.get(dto.childSkuId());

            if (composition == null) {
                ProductSku productSku = getProductSku(dto.childSkuId());
                validateProductSkuForComposition(productSku);

                ProductComposition newComposition = ProductComposition.create(
                    product,
                    productSku,
                    dto.quantity(),
                    dto.sortOrder()
                );
                compositionRepository.save(newComposition);
            } else {
                composition.update(dto.quantity(), dto.sortOrder());
            }
        }
    }

    public void deleteCompositions(Product product) {
        compositionRepository.deleteAllByParentProductId(product.getId());
    }


    // ===== 유효성 검사 메서드 ====

    public void validateDeletableSku(Product product) {
        if (product.isComplete()) return;

        if(compositionRepository.isSkuReferencedByOther(product.getId())) {
            throw new ServiceException(ProductErrorCode.PRODUCT_SKU_REFERENCED_BY_OTHER);
        }
    }

    private void validateProductSkuForComposition(ProductSku productSku) {
        if (productSku.isArchived()) {
            throw new ServiceException(ProductErrorCode.ARCHIVED_SKU);
        }

        if (productSku.getProduct().isComplete()) {
            throw new ServiceException(ProductErrorCode.CHILD_SKU_ONLY_COMPONENT);
        }
    }

    public void validateSkuOwnedByProduct(Long productId, Long skuId) {
        if (!productSkuRepository.existsByIdAndProductId(productId, skuId)) {
            throw new ServiceException(ProductErrorCode.SKU_NOT_BELONGS_TO_PRODUCT);
        }
    }


    // ===== 헬퍼 메서드 ====

    private ProductSku getProductSku(Long skuId) {
        return productSkuRepository.findById(skuId)
            .orElseThrow(() -> new ServiceException(ProductErrorCode.NOT_FOUND_SKU));
    }

    private List<ProductSku> getProductSkus(Long productId) {
        return productSkuRepository.findByProductId(productId);
    }

    private List<ProductComposition> getProductCompositions(Long productId) {
        return compositionRepository.findByParentProductId(productId);
    }
}
