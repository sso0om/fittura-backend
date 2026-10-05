package com.fittura.domain.order.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "장바구니 주문 미리보기 요청 DTO")
public record CartOrderPreviewReqDto(
    @Schema(example = "[1, 2, 3]")
    @NotNull @Size(min = 1)
    List<@NotNull Long> cartItemIds,

    @Schema(description = "배송지 ID (미등록 회원은 null)", example = "1")
    Long addressId
) {
}
