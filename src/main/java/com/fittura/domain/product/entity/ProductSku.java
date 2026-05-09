package com.fittura.domain.product.entity;

import com.fittura.domain.product.constant.SkuStatus;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_skus")
public class ProductSku extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String skuCode;

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
}
