package com.fittura.domain.order.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressCreateReqDto(
    @Schema(example = "John")
    @NotBlank @Size(max = 100)
    String receiverName,

    @Schema(example = "021231234")
    @NotBlank @Size(min = 9, max = 20)
    String phoneNumber,

    @Schema(example = "12345")
    @NotBlank @Pattern(regexp = "\\d{5}")
    String zipCode,

    @Schema(example = "서울특별시 중구 서소문로 127")
    @NotBlank
    String address,

    @Schema(example = "시청역")
    String addressDetail,

    @Schema(example = "서울특별시")
    @NotBlank @Size(max = 20)
    String sido,

    @Schema(example = "중구")
    @NotBlank @Size(max = 20)
    String sigungu,

    @Schema(example = "문앞에 놓아주세요.")
    String deliveryMemo
) {
}
