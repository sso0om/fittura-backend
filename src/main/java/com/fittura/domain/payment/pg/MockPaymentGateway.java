package com.fittura.domain.payment.pg;

import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.constant.PgProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PgProvider pgProvider() {
        return PgProvider.TOSS;
    }

    @Override
    public PgPaymentResponse getPayment(String paymentKey) {
        return new PgPaymentResponse(
            paymentKey,
            "MOCK_PAYMENT_NUMBER_123",
            PaymentStatus.APPROVED,
            10000L,
            LocalDateTime.now(),
            "{mock raw response}",
            new PgCardResponse(
                "11", "1234****", 0, false, "00000000"
            )
        );
    }
}
