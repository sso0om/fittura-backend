package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.sku.dto.response.SkuWithStockResDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "제품 응답 DTO (관리자용)")
public record ProductWithAllResDto(
    Long id,
    String name,
    String description,
    ProductType productType,
    ProductStatus status,
    Long basePrice,
    Double weight,
    Double width,
    Double height,
    Double depth,
    List<SkuWithStockResDto> skus,
    List<ProductAttributeResDto> attributes,
    List<CompositionResDto> compositions
){
    // Projection 전용 생성자
    public ProductWithAllResDto(
        Long id, String name, String description,
        ProductType productType, ProductStatus status,
        Long basePrice, Double weight, Double width,
        Double height, Double depth
    ) {
        this(id, name, description, productType, status,
            basePrice, weight, width, height, depth,
            List.of(), List.of(), List.of());
    }
}
