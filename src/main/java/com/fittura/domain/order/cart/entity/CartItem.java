package com.fittura.domain.order.cart.entity;

import com.fittura.domain.order.cart.error.CartErrorCode;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Getter
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

    private static final int MAX_QUANTITY = 999;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSku productSku;

    @Column(nullable = false)
    private Integer quantity;

    public static CartItem create(Cart cart, ProductSku productSku, Integer quantity) {
        Objects.requireNonNull(cart, "cart must not be null");
        Objects.requireNonNull(productSku, "productSku must not be null");
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("quantity must be greater than or equal to 1");
        }

        CartItem cartItem = new CartItem();
        cartItem.cart = cart;
        cartItem.productSku = productSku;
        cartItem.quantity = quantity;
        return cartItem;
    }

    public void addQuantity(Integer quantity) {
        validateQuantity(quantity);
        this.quantity += quantity;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ServiceException(CartErrorCode.QUANTITY_MUST_BE_POSITIVE);
        }
        if (quantity > MAX_QUANTITY) {
            throw new ServiceException(CartErrorCode.QUANTITY_EXCEEDED);
        }
    }
}
