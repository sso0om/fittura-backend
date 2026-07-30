package com.fittura.domain.order.order.dto.request;

import com.fittura.domain.order.order.constant.ClaimReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ClaimOrderReqDto(

    @Valid @NotNull @Size(min = 1)
    List<ClaimItemReqDto> claimItems,

    @NotNull
    ClaimReason reason,

    @NotNull
    String reasonDetail
) {
}
