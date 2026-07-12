package com.fittura.domain.order.cart.entity;

import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "carts")
@NoArgsConstructor(access = PROTECTED)
public class Cart extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long memberId;

    public static Cart create(Long memberId) {
        Objects.requireNonNull(memberId, "member id must not be null");

        Cart cart = new Cart();
        cart.memberId = memberId;
        return cart;
    }
}
