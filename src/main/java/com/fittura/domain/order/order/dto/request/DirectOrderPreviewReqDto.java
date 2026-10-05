package com.fittura.domain.order.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "바로 주문 전 조회 요청 DTO")
public record DirectOrderPreviewReqDto(
    @NotNull @Size(min = 1)
    List<@NotNull @Valid OrderSkuReqDto> orderSkus,

    @Schema(description = "배송지 ID (미등록 회원은 null)", example = "1")
    Long addressId
) {
}
