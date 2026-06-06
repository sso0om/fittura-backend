package com.fittura.domain.order.cart.entity;

import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "carts")
@NoArgsConstructor(access = PROTECTED)
public class Cart extends BaseEntity {

    @Column(nullable = false)
    private Long memberId;
}
