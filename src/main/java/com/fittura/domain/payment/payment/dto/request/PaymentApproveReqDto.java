package com.fittura.domain.payment.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "결제 승인 요청 DTO")
public record PaymentApproveReqDto(
    @NotBlank @Size(max = 200)
    String paymentKey
) {
}
