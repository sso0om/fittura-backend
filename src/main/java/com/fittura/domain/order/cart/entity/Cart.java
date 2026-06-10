package com.fittura.domain.order.cart.entity;

import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
    name = "carts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cart_member",
            columnNames = {"member_id"}
        )
    }
)
@NoArgsConstructor(access = PROTECTED)
public class Cart extends BaseEntity {

    @Column(nullable = false)
    private Long memberId;

    public static Cart create(Long memberId) {
        Cart cart = new Cart();
        cart.memberId = memberId;
        return cart;
    }
}
