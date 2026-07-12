package com.fittura.domain.order.order.entity;

import com.fittura.domain.order.order.constant.OrderStatus;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class Order extends BaseEntity {

    private static final long DELIVERY_BASE_FEE = 4000L;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private Long discountAmount;

    @Column(nullable = false)
    private Long pointUsedAmount;

    @Column(nullable = false)
    private Long deliveryFee;

    @Column(nullable = false)
    private Long finalAmount;

    @Column(nullable = false)
    private LocalDateTime orderDate;


    // ===== 생성 =====

    public static Order create(Long memberId, Long pointUsedAmount) {
        Objects.requireNonNull(memberId, "memberId must not be null");
        validateAmount(pointUsedAmount);

        LocalDateTime now = LocalDateTime.now();
        String orderNumber = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            + "-"
            + UUID.randomUUID().toString().substring(0, 8);

        return Order.builder()
            .memberId(memberId)
            .orderNumber(orderNumber)
            .status(OrderStatus.PENDING)
            .totalAmount(0L)
            .discountAmount(0L)
            .pointUsedAmount(pointUsedAmount)
            .deliveryFee(DELIVERY_BASE_FEE)
            .finalAmount(0L)
            .orderDate(now)
            .build();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        totalAmount += item.getUnitPrice() * item.getQuantity();
        discountAmount += item.getDiscountAmount();
    }

    public void calcDiscountAmount(Long discountAmount) {
        // TODO: promotion 기능 때 반영 예정
        this.discountAmount = discountAmount;
    }

    public void calcDeliveryFee() {
        // TODO: delivery 기능 때 반영 예정
    }

    public void calcFinalAmount() {
        finalAmount = totalAmount - discountAmount - pointUsedAmount + deliveryFee;
    }

    private static void validateAmount(Long amount) {
        if (amount == null || amount < 0) {
            throw new ServiceException(OrderErrorCode.AMOUNT_MUST_BE_POSITIVE);
        }
    }
}
