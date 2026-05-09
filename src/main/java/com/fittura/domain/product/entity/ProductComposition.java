package com.fittura.domain.product.entity;

import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "product_compositions")
@NoArgsConstructor(access = PROTECTED)
public class ProductComposition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_id", nullable = false)
    private Product parentProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_sku_id", nullable = false)
    private ProductSku childSku;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
