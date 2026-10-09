package com.fittura.domain.payment.pg;

import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.constant.PgProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile({"dev", "test"})
public class MockPaymentGateway implements PaymentGateway {

    private final Map<String, PgPaymentResponse> responseMap = new ConcurrentHashMap<>();

    @Override
    public PgProvider pgProvider() {
        return PgProvider.TOSS;
    }

    @Override
    public PgPaymentResponse confirm(PgConfirmCommand command) {
        PgPaymentResponse stubbed = responseMap.get(command.paymentKey());
        return stubbed != null ? stubbed : approved(command);
    }


    // ===== stub =====

    public void stub(String paymentKey, PgPaymentResponse response) {
        responseMap.put(paymentKey, response);
    }

    public void stubRejected(String paymentKey, String errorCode, String errorMessage) {
        stub(paymentKey, PgPaymentResponse.rejected(
            paymentKey, null, errorJson(errorCode, errorMessage), errorCode, errorMessage
        ));
    }

    public void stubUnknown(String paymentKey, String errorCode) {
        stub(paymentKey, PgPaymentResponse.unknown(
            paymentKey, null, errorJson(errorCode, null), errorCode
        ));
    }

    public void clear() {
        responseMap.clear();
    }


    // ===== 승인 =====

    private PgPaymentResponse approved(PgConfirmCommand command) {
        return new PgPaymentResponse(
            command.paymentKey(),
            command.paymentNumber(),
            PaymentStatus.APPROVED,
            command.amount(),
            LocalDateTime.now(),
            "{\"mock\":\"approved\"}",
            null,
            null,
            new PgCardResponse("11", "1234-56**-****-5678", 0, false, "00000000")
        );
    }


    // ===== 헬퍼 메서드 =====

    private String errorJson(String errorCode, String errorMessage) {
        return "{\"code\":\"%s\",\"message\":\"%s\"}".formatted(errorCode, errorMessage);
    }
}
