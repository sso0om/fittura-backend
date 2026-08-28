package com.fittura.domain.payment.pg;

import com.fittura.domain.payment.payment.constant.PgProvider;

public interface PaymentGateway {
    PgProvider pgProvider();
    PgPaymentResponse getPayment(String paymentKey);
}
