package com.fittura.domain.payment.payment.entity;

import com.fittura.domain.payment.payment.constant.PgFailureType;
import com.fittura.domain.payment.payment.constant.PgProvider;
import com.fittura.domain.payment.payment.constant.PaymentMethod;
import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.error.PaymentErrorCode;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, unique = true, length = 50)
    private String paymentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PgProvider pgProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(unique = true, length = 100)
    private String pgTransactionId;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private Long cancelledAmount;

    @Enumerated(EnumType.STRING)
    private PgFailureType failureType;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;

    @Column(nullable = false)
    private LocalDateTime requestedDate;

    private LocalDateTime approvedDate;

    private LocalDateTime cancelledDate;


    // ===== 생성 =====

    public static Payment create(Long orderId, PgProvider pgProvider, PaymentMethod paymentMethod,Long totalAmount) {
        Objects.requireNonNull(orderId, "orderID must not be null");

        LocalDateTime now = LocalDateTime.now();
        String paymentNumber = "PAY-"
            + now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            + "-"
            + UUID.randomUUID().toString().substring(0, 8);

        return Payment.builder()
            .orderId(orderId)
            .paymentNumber(paymentNumber)
            .status(PaymentStatus.PENDING)
            .pgProvider(pgProvider)
            .paymentMethod(paymentMethod)
            .totalAmount(totalAmount)
            .cancelledAmount(0L)
            .requestedDate(now)
            .build();
    }

    public void approve(String pgTransactionId, LocalDateTime approvedDate, String rawResponse) {
        Objects.requireNonNull(approvedDate, "approvedDate must not be null");
        Objects.requireNonNull(rawResponse, "rawResponse must not be null");
        if (approvedDate.isBefore(requestedDate)) throw new ServiceException(PaymentErrorCode.NOT_VALID_PG);

        this.status = PaymentStatus.APPROVED;
        this.pgTransactionId = pgTransactionId;
        this.approvedDate = approvedDate;
        this.rawResponse = rawResponse;
    }

    public void validatePayable() {
        if (this.status != PaymentStatus.PENDING) {
            throw new ServiceException(PaymentErrorCode.NOT_PAYABLE_STATUS);
        }
    }
}
