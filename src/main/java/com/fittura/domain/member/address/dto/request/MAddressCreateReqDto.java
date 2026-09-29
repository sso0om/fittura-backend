package com.fittura.domain.member.address.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "나의 배송지 생성 요청 DTO")
public record MAddressCreateReqDto(
    @Schema(example = "우리집")
    @NotBlank @Size(max = 50)
    String addressName,

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
    @NotBlank @Size(max = 255)
    String address,

    @Schema(example = "시청역")
    @Size(max = 255)
    String addressDetail,

    @Schema(example = "서울특별시")
    @NotBlank @Size(max = 20)
    String sido,

    @Schema(example = "중구")
    @NotBlank @Size(max = 20)
    String sigungu,

    @Schema(example = "true")
    @NotNull
    Boolean defaultAddress
) {
}
