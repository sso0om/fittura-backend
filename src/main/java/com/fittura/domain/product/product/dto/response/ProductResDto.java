package com.fittura.domain.product.product.dto.response;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.sku.dto.responseDto.SkuResDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;


@Schema(description = "제품 응답 DTO")
public record ProductResDto(
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
    public static ProductResDto from(Product product) {
        return new ProductResDto(
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
