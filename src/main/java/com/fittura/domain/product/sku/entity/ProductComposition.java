package com.fittura.domain.product.sku.entity;

import com.fittura.domain.product.product.entity.Product;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "product_compositions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_product_composition_product_id_sku_id",
            columnNames = {"parent_product_id", "child_sku_id"}
        )
    }
)
@NoArgsConstructor(access = PROTECTED)
public class ProductComposition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_id", nullable = false)
    private Product parentProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_sku_id", nullable = false)
    private ProductSku childSku;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public static ProductComposition create(
        Product parentProduct,
        ProductSku childSku,
        Integer quantity,
        Integer sortOrder
    ) {
        Objects.requireNonNull(parentProduct, "parent product must not be null");
        Objects.requireNonNull(childSku, "child  sku must not be null");
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be greater than or equal to 1");
        }

        ProductComposition productComposition = new ProductComposition();
        productComposition.parentProduct = parentProduct;
        productComposition.childSku = childSku;
        productComposition.quantity = quantity;
        productComposition.sortOrder = sortOrder;

        return productComposition;
    }

    public void update(Integer quantity, Integer sortOrder) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be greater than or equal to 1");
        }

        this.quantity = quantity;
        this.sortOrder = sortOrder;
    }
}
