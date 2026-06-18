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
@Table(name = "order_address")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class OrderAddress extends BaseEntity {

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 100)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 5)
    private String zipCode;

    @Column(nullable = false)
    private String address;

    @Column
    private String addressDetail;

    @Column(nullable = false, length = 20)
    private String sido;

    @Column(nullable = false, length = 20)
    private String sigungu;

    @Column
    private String deliveryMemo;


    public static OrderAddress create(
        Order order,
        String receiverName,
        String phoneNumber,
        String zipCode,
        String address,
        String addressDetail,
        String sido,
        String sigungu,
        String deliveryMemo
    ) {
        Objects.requireNonNull(order, "order must not be null");

        return OrderAddress.builder()
            .order(order)
            .receiverName(receiverName)
            .phoneNumber(phoneNumber)
            .zipCode(zipCode)
            .address(address)
            .addressDetail(addressDetail)
            .sido(sido)
            .sigungu(sigungu)
            .deliveryMemo(deliveryMemo)
            .build();
    }
}
