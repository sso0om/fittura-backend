package com.fittura.domain.product.sku.entity;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.sku.constant.SkuStatus;
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
    private Long price;

    @Column
    private Long salePrice;

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
        Long price,
        Long salePrice,
        Integer stockQuantity,
        Color color,
        Material material
    ) {
        Objects.requireNonNull(product, "product must not be null");
        validateSalePrice(price, salePrice);

        ProductSku productSku = ProductSku.builder()
            .product(product)
            .price(price)
            .salePrice(salePrice)
            .stockQuantity(stockQuantity)
            .reservedQuantity(0)
            .status(SkuStatus.ACTIVE)
            .color(color)
            .material(material)
            .build();

        product.addProductSku(productSku);

        return productSku;
    }

    public void update(Long price, Long salePrice, Integer stockQuantity, Color color, Material material) {
        validateSalePrice(price, salePrice);

        this.price = price;
        this.salePrice = salePrice;
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


    // ===== 상태 =====

    public void soldOut() {
        this.status = SkuStatus.SOLDOUT;
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


    // ===== getter =====

    public String getSkuIdentifier() {
        return Stream.of(
                color != null ? color.getName() : null,
                material != null ? material.getName() : null
            )
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining(" / "));
    }

    public Long getDiscountRate() {
        if (salePrice == null) return 0L;
        return (price - salePrice) / price;
    }

    public Long getEffectivePrice() {
        return salePrice == null ? price : salePrice;
    }


    // ===== 유효성 검사 =====

    private static void validateSalePrice(Long price, Long salePrice) {
        if (salePrice != null && price <= salePrice) {
            throw new ServiceException(ProductErrorCode.SALE_PRICE_LESS_THAN_PRICE);
        }
    }
}
