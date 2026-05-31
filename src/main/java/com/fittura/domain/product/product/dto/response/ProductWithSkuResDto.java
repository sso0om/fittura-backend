package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.sku.dto.response.SkuResDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "제품 응답 DTO")
public record ProductWithSkuResDto(
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
    List<SkuResDto> skus
){
    // Projection 전용 생성자
    public ProductWithSkuResDto(
        Long id, String name, String description,
        ProductType productType, ProductStatus status,
        Long basePrice, Double weight, Double width,
        Double height, Double depth
    ) {
        this(id, name, description, productType, status,
            basePrice, weight, width, height, depth, List.of());
    }

    // 엔티티 변환용
    public static ProductWithSkuResDto from(Product product) {
        return new ProductWithSkuResDto(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getProductType(),
            product.getStatus(),
            product.getBasePrice(),
            product.getDimension().getWeight(),
            product.getDimension().getWidth(),
            product.getDimension().getHeight(),
            product.getDimension().getDepth(),
            product.getProductSkus().stream()
                .map(SkuResDto::from)
                .toList()
        );
    }
}
