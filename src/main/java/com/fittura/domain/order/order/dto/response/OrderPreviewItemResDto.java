package com.fittura.domain.order.order.dto.response;

import com.fittura.domain.delivery.delivery.constant.DeliveryType;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.entity.ProductImage;
import com.fittura.domain.product.sku.entity.ProductSku;

public record OrderPreviewItemResDto(
    /* product */
    Long productId,
    String productName,
    String mainImageUrl,
    DeliveryType deliveryType,
    /* sku */
    Long skuId,
    String color,
    String material,
    Long originalPrice,
    Long salePrice,
    Long discountRate,
    Integer quantity,
    Long itemTotalAmount
) {
    public static OrderPreviewItemResDto from(ProductSku sku, int quantity) {
        Product product = sku.getProduct();
        ProductImage mainImage = product.getMainImage();

        return new OrderPreviewItemResDto(
            product.getId(),
            product.getName(),
            mainImage != null ? mainImage.getImageUrl() : null,
            product.getDeliveryType(),
            sku.getId(),
            sku.getColor() != null ? sku.getColor().getName() : null,
            sku.getMaterial() != null ? sku.getMaterial().getName() : null,
            sku.getOriginalPrice(),
            sku.getSalePrice(),
            sku.getDiscountRate(),
            quantity,
            sku.getSalePrice() * quantity
        );
    }
}
