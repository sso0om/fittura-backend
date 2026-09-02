package com.fittura.domain.payment.payment.dto.request;

import com.fittura.domain.payment.payment.constant.PaymentMethod;
import com.fittura.domain.payment.payment.constant.PgProvider;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 준비 요청 DTO")
public record PaymentPrepareReqDto(
    Long orderId,
    PgProvider pgProvider,
    PaymentMethod paymentMethod
) {
}
