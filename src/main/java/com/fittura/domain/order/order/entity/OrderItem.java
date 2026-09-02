package com.fittura.domain.order.order.entity;

import com.fittura.domain.order.order.constant.OrderItemStatus;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.global.exception.ServiceException;
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
@Table(
    name = "order_items",
    indexes = {
        @Index(
            name = "idx_order_items_delivery_id",
            columnList = "delivery_id"
        )
    }
)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class OrderItem extends BaseEntity {

    private static final int MAX_QUANTITY = 999;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSku sku;

    @Builder.Default
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL)
    private List<ClaimItem> claimItems = new ArrayList<>();

    @Column
    private Long deliveryId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, length = 100)
    private String skuIdentifier;

    @Column(nullable = false)
    private Long unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long discountAmount;

    @Column(nullable = false)
    private Long itemTotalAmount;

    @Column(nullable = false)
    private OrderItemStatus status;


    // ===== 생성 =====

    public static OrderItem create(
        Order order,
        ProductSku sku,
        Integer quantity
    ) {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(sku, "sku must not be null");
        validateQuantity(quantity);

        OrderItem orderItem = OrderItem.builder()
            .order(order)
            .sku(sku)
            .productName(sku.getProduct().getName())
            .skuIdentifier(sku.getSkuIdentifier())
            .unitPrice(sku.getPrice())
            .quantity(quantity)
            .discountAmount(0L)
            .itemTotalAmount(sku.getPrice() * quantity)
            .status(OrderItemStatus.ORDERED)
            .build();

        order.addItem(orderItem);

        return orderItem;
    }

    public void addClaimItem(ClaimItem claimItem) {
        claimItems.add(claimItem);
    }

    public void calcDiscountAmount(Long discountAmount) {
        // TODO: promotion 기능 때 반영 예정
        this.discountAmount = discountAmount;
        this.itemTotalAmount = itemTotalAmount - discountAmount;
    }

    public Long calcRefundAmount(int claimQuantity) {
        int remaining = quantity - getTotalClaimQuantity();

        if (claimQuantity == remaining) {
            return itemTotalAmount - getTotalRefundedAmount();
        }
        return itemTotalAmount * claimQuantity / quantity;
    }

    public void assignDelivery(Long deliveryId) {
        // TODO: delivery의 order와 orderItem의 Order 일치 여부는 delivery 생성 로직에서 진행
        this.deliveryId = deliveryId;
    }


    // ===== 상태 =====

    public void requestCancel() {
        this.status = OrderItemStatus.CANCEL_REQUESTED;
    }

    public void reflectCancel() {
        if (quantity == getTotalClaimQuantity()) {
            this.status = OrderItemStatus.CANCELLED;
        }
    }

    public boolean isOrdered() {
        return status == OrderItemStatus.ORDERED;
    }

    public boolean isCanceled() {
        return status == OrderItemStatus.CANCELLED;
    }

    // ===== 유효성 검증 =====

    public boolean isQuantityValid(Integer claimQuantity) {
        return quantity - getTotalClaimQuantity() - claimQuantity >= 0;
    }

    private static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ServiceException(OrderErrorCode.QUANTITY_MUST_BE_POSITIVE);
        }
        if (quantity > MAX_QUANTITY) {
            throw new ServiceException(OrderErrorCode.QUANTITY_EXCEEDED);
        }
    }


    // ===== 헬퍼 메서드 =====

    private int getTotalClaimQuantity() {
        return claimItems.stream()
            .filter(ci -> ci.getClaim().isConfirmed())
            .mapToInt(ClaimItem::getQuantity)
            .sum();
    }

    private Long getTotalRefundedAmount() {
        return claimItems.stream()
            .filter(ci -> ci.getClaim().isConfirmed())
            .mapToLong(ClaimItem::getRefundAmount)
            .sum();
    }
}
