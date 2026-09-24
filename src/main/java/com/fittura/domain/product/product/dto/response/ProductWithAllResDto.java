package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.sku.dto.response.SkuWithStockResDto;
import com.fittura.domain.product.sku.util.PriceCalculator;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "제품 응답 DTO (관리자용)")
public record ProductWithAllResDto(
    Long id,
    String name,
    String description,
    ProductType productType,
    DeliveryType deliveryType,
    Long deliveryFee,
    ProductStatus status,
    Long basePrice,
    Long baseSalePrice,
    Long discountRate,
    Double weight,
    Double width,
    Double height,
    Double depth,
    boolean isSoldOut,
    List<SkuWithStockResDto> skus,
    List<ProductAttributeResDto> attributes,
    List<CompositionResDto> compositions
) {
    // Projection 전용 생성자
    public ProductWithAllResDto(
        Long id, String name, String description,
        ProductType productType, DeliveryType deliveryType, ProductStatus status,
        Long basePrice, Long baseSalePrice,
        Double weight, Double width, Double height, Double depth, boolean isSoldOut
    ) {
        this(id, name, description,
            productType, deliveryType, deliveryType.getBaseFee(), status,
            basePrice, baseSalePrice, PriceCalculator.discountRate(basePrice, baseSalePrice),
            weight, width, height, depth, isSoldOut, List.of(), List.of(), List.of());
    }
}
