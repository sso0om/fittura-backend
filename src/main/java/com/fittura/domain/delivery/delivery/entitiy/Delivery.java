package com.fittura.domain.delivery.delivery.entitiy;

import com.fittura.domain.delivery.delivery.constant.DeliveryStatus;
import com.fittura.domain.product.product.constant.DeliveryType;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "deliveries")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class Delivery extends BaseEntity {

    @Column(nullable = false)
    private Long orderId;

    // TODO: driver, zone

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(nullable = false)
    private Long deliveryFee;

    @Column
    private LocalDateTime shippedDate;

    @Column
    private LocalDateTime deliveredDate;


    public static Delivery create(Long orderId, DeliveryType deliveryType, Long deliveryFee) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(deliveryType, "deliveryType must not be null");

        return Delivery.builder()
            .orderId(orderId)
            .deliveryType(deliveryType)
            .status(DeliveryStatus.READY)
            .deliveryFee(deliveryFee)
            .build();
    }

    public void cancel() {
        this.status = DeliveryStatus.CANCELLED;
    }
}
