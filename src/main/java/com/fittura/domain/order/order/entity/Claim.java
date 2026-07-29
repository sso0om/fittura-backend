package com.fittura.domain.order.order.entity;

import com.fittura.domain.order.order.constant.ClaimReason;
import com.fittura.domain.order.order.constant.ClaimStatus;
import com.fittura.domain.order.order.constant.ClaimType;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "claim")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class Claim extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder.Default
    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    private List<ClaimItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimReason reason;

    @Column
    private String reasonDetail;

    @Column(nullable = false)
    private Long refundAmount;


    // ===== 생성 =====

    public static Claim create(
        Order order,
        ClaimType claimType,
        ClaimReason reason,
        String reasonDetail
    ) {
        Objects.requireNonNull(order, "order must not be null");

        return Claim.builder()
            .order(order)
            .claimType(claimType)
            .status(ClaimStatus.REQUESTED)
            .reason(reason)
            .reasonDetail(reasonDetail)
            .refundAmount(0L)
            .build();
    }

    public void addItem(ClaimItem item) {
        items.add(item);
        refundAmount += item.getRefundAmount();
    }

    public boolean isConfirmed() {
        return status != ClaimStatus.REJECTED;
    }
}
