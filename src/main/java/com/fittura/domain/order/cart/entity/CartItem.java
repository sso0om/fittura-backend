package com.fittura.domain.order.cart.entity;

import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cart_item_cart_sku",
            columnNames = {"cart_id", "sku_id"}
        )
    }
)
@NoArgsConstructor(access = PROTECTED)
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSku productSku;

    @Column(nullable = false)
    private Integer quantity;
}
