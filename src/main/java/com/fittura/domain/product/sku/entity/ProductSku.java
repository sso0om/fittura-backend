package com.fittura.domain.product.sku.entity;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.util.PriceCalculator;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "product_skus")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class ProductSku extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long originalPrice;

    @Column
    private Long discountPrice;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Integer reservedQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkuStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colorId")
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    // ===== 생성 =====

    public static ProductSku create(
        Product product,
        Long originalPrice,
        Long discountPrice,
        Integer stockQuantity,
        Color color,
        Material material
    ) {
        Objects.requireNonNull(product, "product must not be null");
        validateDiscountPrice(originalPrice, discountPrice);

        ProductSku productSku = ProductSku.builder()
            .product(product)
            .originalPrice(originalPrice)
            .discountPrice(discountPrice)
            .stockQuantity(stockQuantity)
            .reservedQuantity(0)
            .status(SkuStatus.ACTIVE)
            .color(color)
            .material(material)
            .build();

        product.addProductSku(productSku);

        return productSku;
    }

    public void update(Long originalPrice, Long discountPrice, Integer stockQuantity, Color color, Material material) {
        validateDiscountPrice(originalPrice, discountPrice);

        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.stockQuantity = stockQuantity;
        this.color = color;
        this.material = material;
    }

    public void reserveQuantity(Integer quantity) {
        if (!isStockValid(quantity)) {
            throw new ServiceException(ProductErrorCode.STOCK_NOT_VALID);
        }
        this.reservedQuantity += quantity;
    }


    // ===== status =====

    public void pause() {
        this.status = SkuStatus.PAUSED;
    }

    public void discontinue() {
        this.status = SkuStatus.DISCONTINUED;
    }

    public void archive() {
        this.status = SkuStatus.ARCHIVED;
    }

    public boolean isStockValid(Integer orderQuantity) {
        if (orderQuantity == null || orderQuantity < 1) {
            return false;
        }
        return this.stockQuantity - reservedQuantity - orderQuantity >= 0;
    }

    public boolean isActive() {
        return this.status == SkuStatus.ACTIVE;
    }

    public boolean isArchived() {
        return status == SkuStatus.ARCHIVED;
    }

    public boolean isSellable() {
        return isActive() && product.isActive();
    }


    // ===== getter =====

    public String getSkuIdentifier() {
        return Stream.of(
                color != null ? color.getName() : null,
                material != null ? material.getName() : null
            )
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining(" / "));
    }

    // 판매가 = 할인가가 있으면 할인가, 없으면 정가
    public Long getSalePrice() {
        return PriceCalculator.salePrice(originalPrice, discountPrice);
    }

    public Long getDiscountRate() {
        return PriceCalculator.discountRate(originalPrice, getSalePrice());
    }


    // ===== 유효성 검사 =====

    private static void validateDiscountPrice(Long originalPrice, Long discountPrice) {
        if (discountPrice != null && originalPrice <= discountPrice) {
            throw new ServiceException(ProductErrorCode.DISCOUNT_PRICE_LESS_THAN_ORIGINAL_PRICE);
        }
    }
}
