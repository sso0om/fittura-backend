package com.fittura.domain.payment.payment.dto.request;

import com.fittura.domain.payment.payment.constant.PaymentMethod;
import com.fittura.domain.payment.payment.constant.PgProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "결제 준비 요청 DTO")
public record PaymentPrepareReqDto(
    @NotNull @Positive
    Long orderId,

    @NotNull
    PgProvider pgProvider,

    @NotNull
    PaymentMethod paymentMethod
) {
}
