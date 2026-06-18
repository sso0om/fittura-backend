package com.fittura.domain.order.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressCreateReqDto(
    @Schema(example = "John")
    @NotNull @Size(min = 1, max = 100)
    String receiverName,

    @Schema(example = "021231234")
    @NotNull @Size(min = 9, max = 20)
    String phoneNumber,

    @Schema(example = "12345")
    @NotNull @Pattern(regexp = "\\d{5}")
    String zipCode,

    @Schema(example = "서울특별시 중구 서소문로 127")
    @NotNull
    String address,

    @Schema(example = "시청역")
    String addressDetail,

    @Schema(example = "서울특별시")
    @NotNull
    String sido,

    @Schema(example = "중구")
    @NotNull
    String sigungu,

    @Schema
    String deliveryMemo
) {
}
