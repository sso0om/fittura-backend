package com.fittura.domain.order.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateReqDto(

    @Schema(example = "1L")
    @NotNull @Size(min = 1)
    List<Long> cartItems,

    @Schema(example = "5000L")
    @NotNull @Positive
    Long pointUsedAmount,

    // TODO: Promotion 추가 시 적용 쿠폰 정보 추가 필요

    @Valid @NotNull
    AddressCreateReqDto orderAddress
) {
}
