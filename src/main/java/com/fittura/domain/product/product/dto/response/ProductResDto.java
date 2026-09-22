package com.fittura.domain.product.product.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.sku.util.PriceCalculator;

import java.time.LocalDateTime;

public record ProductResDto(
    Long id,
    String name,
    ProductType productType,
    DeliveryType deliveryType,
    Long basePrice,
    Long baseSalePrice,
    Long discountRate,
    ProductStatus status,
    @JsonIgnore LocalDateTime createdDate,
    boolean isSoldOut,
    String mainImageUrl
) {
   public ProductResDto(
       Long id, String name, ProductType productType, DeliveryType deliveryType,
       Long basePrice, Long baseSalePrice,
       ProductStatus status, LocalDateTime createdDate, boolean isSoldOut, String mainImageUrl
   ) {
       this(id, name, productType, deliveryType,
           basePrice, baseSalePrice, PriceCalculator.discountRate(basePrice, baseSalePrice),
           status, createdDate, isSoldOut, mainImageUrl);
   }
}
