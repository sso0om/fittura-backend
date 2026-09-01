package com.fittura.domain.payment.payment.support;

import com.fittura.domain.payment.payment.constant.PaymentMethod;
import com.fittura.domain.payment.payment.constant.PgProvider;
import com.fittura.domain.payment.payment.entity.Payment;
import org.springframework.test.util.ReflectionTestUtils;

public class PaymentFixture {

    private PaymentFixture() {}

    public static Payment payment(Long orderId) {
        return Payment.create(orderId, PgProvider.TOSS, PaymentMethod.CARD, 10000L);
    }

    public static Payment payment(Long orderId, Long totalAmount) {
        return Payment.create(orderId, PgProvider.TOSS, PaymentMethod.CARD, totalAmount);
    }

    public static Payment payment(Long orderId, PgProvider pgProvider, PaymentMethod paymentMethod, Long totalAmount) {
        return Payment.create(orderId, pgProvider, paymentMethod, totalAmount);
    }

    public static Payment paymentWithId(Long id, Long orderId) {
        Payment payment = payment(orderId);
        ReflectionTestUtils.setField(payment, "id", id);
        return payment;
    }
}
