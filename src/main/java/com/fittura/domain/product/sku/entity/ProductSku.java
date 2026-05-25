package com.fittura.domain.product.sku.entity;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "product_skus")
@NoArgsConstructor(access = PROTECTED)
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

    @Column(nullable = false)
    private Double weight = 0.0;

    @OneToMany(mappedBy = "sku", cascade = CascadeType.PERSIST)
    private List<SkuAttribute> attributes = new ArrayList<>();


    // ===== 생성 =====

    public static ProductSku create(
        Product product,
        Long price,
        Integer stockQuantity,
        String color,
        String material,
        Double weight
    ) {
        Objects.requireNonNull(product, "product must not be null");

        ProductSku productSku = new ProductSku();
        productSku.product = product;
        productSku.price = price;
        productSku.stockQuantity = stockQuantity;
        productSku.status = SkuStatus.ACTIVE;

        productSku.color = color;
        productSku.material = material;
        productSku.weight = weight;

        return productSku;
    }

    public void addAttribute(SkuAttribute skuAttribute) {
        attributes.add(skuAttribute);
    }

    public boolean isArchived() {
        return status == SkuStatus.ARCHIVED;
    }
}
