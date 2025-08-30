package com.fittura.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInReqDto(
    @Email @NotBlank @Size(min = 6, max = 254)
    String email,
    @NotBlank @Size(min = 8, max = 60) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password
) {
}
