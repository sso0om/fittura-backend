package com.fittura.domain.payment.payment.dto.response;

import com.fittura.domain.payment.payment.constant.PaymentMethod;
import com.fittura.domain.payment.payment.constant.PgProvider;
import com.fittura.domain.payment.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 준비 응답 DTO")
public record PaymentPrepareResDto(
    Long orderId,
    Long paymentId,
    String paymentNumber,
    PgProvider pgProvider,
    PaymentMethod paymentMethod,
    Long totalAmount
) {
    public static PaymentPrepareResDto from(Payment payment) {
        return new PaymentPrepareResDto(
            payment.getOrderId(),
            payment.getId(),
            payment.getPaymentNumber(),
            payment.getPgProvider(),
            payment.getPaymentMethod(),
            payment.getTotalAmount()
        );
    }
}
