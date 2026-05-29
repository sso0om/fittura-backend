package com.fittura.domain.product.sku.entity;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

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

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Integer reservedQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkuStatus status;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String material;

    // ===== 생성 =====

    public static ProductSku create(
        Product product,
        Long price,
        Integer stockQuantity,
        String color,
        String material
    ) {
        Objects.requireNonNull(product, "product must not be null");

        ProductSku productSku = ProductSku.builder()
            .product(product)
            .price(price)
            .stockQuantity(stockQuantity)
            .reservedQuantity(0)
            .status(SkuStatus.ACTIVE)
            .color(color)
            .material(material)
            .build();

        product.addProductSku(productSku);

        return productSku;
    }

    public boolean isArchived() {
        return status == SkuStatus.ARCHIVED;
    }
}
