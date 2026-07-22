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

    public void calcDiscountAmount(Long discountAmount) {
        // TODO: promotion 기능 때 반영 예정
        this.discountAmount = discountAmount;
        this.itemTotalAmount = itemTotalAmount - discountAmount;
    }

    public void assignDelivery(Long deliveryId) {
        this.deliveryId = deliveryId;
    }

    private static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ServiceException(OrderErrorCode.QUANTITY_MUST_BE_POSITIVE);
        }
        if (quantity > MAX_QUANTITY) {
            throw new ServiceException(OrderErrorCode.QUANTITY_EXCEEDED);
        }
    }
}
