package com.fittura.domain.payment.payment.entity;

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
@Table(name = "payment_cards")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class PaymentCard extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, length = 10)
    private String issuerCode;

    @Column(nullable = false, length = 20)
    private String cardNumberMasked;

    @Column(nullable = false)
    private int installmentMonths;

    @Column(nullable = false)
    private boolean isInterestFree;

    @Column(nullable = false, length = 20)
    private String approvalNumber;


    // ===== 생성 =====

    public static PaymentCard create(
        Payment payment, String issuerCode, String cardNumberMasked,
        int installmentMonths,  boolean isInterestFree , String approvalNumber
    ) {
        Objects.requireNonNull(payment, "Payment must not be null");
        return PaymentCard.builder()
            .payment(payment)
            .issuerCode(issuerCode)
            .cardNumberMasked(cardNumberMasked)
            .installmentMonths(installmentMonths)
            .isInterestFree(isInterestFree)
            .approvalNumber(approvalNumber)
            .build();
    }
}
