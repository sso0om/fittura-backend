package com.fittura.domain.product.entity;

import com.fittura.domain.product.constant.ImageType;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "product_images")
@NoArgsConstructor(access = PROTECTED)
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id")
    private ProductSku sku;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType imageType;

    @Column(nullable = false)
    private int sortOrder = 0;

    @Column(length = 255)
    private String altText;
}
