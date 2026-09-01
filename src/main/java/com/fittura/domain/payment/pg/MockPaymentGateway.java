package com.fittura.domain.payment.pg;

import com.fittura.domain.payment.payment.constant.PgProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockPaymentGateway implements PaymentGateway {

    private final Map<String, PgPaymentResponse> responseMap = new ConcurrentHashMap<>();

    @Override
    public PgProvider pgProvider() {
        return PgProvider.TOSS;
    }

    @Override
    public PgPaymentResponse getPayment(String paymentKey) {
        PgPaymentResponse stubbed = responseMap.get(paymentKey);
        if (stubbed == null) {
            throw new IllegalArgumentException("stub 안 된 paymentKey: " + paymentKey);
        }
        return stubbed;
    }

    public void stub(String paymentKey, PgPaymentResponse response) {
        responseMap.put(paymentKey, response);
    }

    public void clear() {
        responseMap.clear();
    }
}
