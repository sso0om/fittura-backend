package com.fittura.domain.order.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "바로 주문 생성 요청 DTO")
public record DirectOrderCreateReqDto(
    @NotNull
    @Size(min = 1)
    List<@NotNull @Valid OrderSkuReqDto> orderSkus,

    @Schema(example = "5000")
    @NotNull @PositiveOrZero
    Long pointUsedAmount,

    @Schema(example = "1")
    @NotNull
    Long addressId,

    @Schema(example = "문앞에 놓아주세요.")
    @Size(max = 255)
    String deliveryMemo

    // TODO: Promotion 추가 시 적용 쿠폰 정보 추가 필요
) {
}
