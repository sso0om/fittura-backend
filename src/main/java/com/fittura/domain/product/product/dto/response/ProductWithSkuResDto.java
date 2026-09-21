package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.sku.dto.response.SkuResDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "제품 응답 DTO")
public record ProductWithSkuResDto(
    Long id,
    Long categoryId,
    String name,
    String description,
    ProductType productType,
    DeliveryType deliveryType,
    Long deliveryFee,
    ProductStatus status,
    Long basePrice,
    Double weight,
    Double width,
    Double height,
    Double depth,
    boolean isSoldOut,
    List<SkuResDto> skus
) {
    // Projection 전용 생성자
    public ProductWithSkuResDto(
        Long id, Long categoryId, String name, String description,
        ProductType productType, DeliveryType deliveryType, ProductStatus status,
        Long basePrice, Double weight, Double width,
        Double height, Double depth, boolean isSoldOut
    ) {
        this(id, categoryId, name, description, productType, deliveryType, deliveryType.getBaseFee(), status,
            basePrice, weight, width, height, depth, isSoldOut, List.of());
    }
}
