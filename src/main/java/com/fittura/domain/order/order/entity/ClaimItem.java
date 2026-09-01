package com.fittura.domain.order.order.entity;

import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "claim_items")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class ClaimItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long refundAmount;


    // ===== 생성 =====

    public static ClaimItem create(Claim claim, OrderItem orderItem, Integer claimQuantity) {
        Objects.requireNonNull(claim, "claim must not be null");
        Objects.requireNonNull(orderItem, "orderItem must not be null");

        ClaimItem claimItem = ClaimItem.builder()
            .claim(claim)
            .orderItem(orderItem)
            .quantity(claimQuantity)
            .refundAmount(orderItem.calcRefundAmount(claimQuantity))
            .build();

        claim.addItem(claimItem);
        orderItem.addClaimItem(claimItem);

        return claimItem;
    }
}
