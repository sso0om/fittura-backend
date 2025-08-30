package com.fittura.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpReqDto(
    @Email @NotBlank @Size(min = 6, max = 254)
    String email,
    @NotBlank @Size(min = 2, max = 10)
    String name,
    @NotBlank @Size(min = 2, max = 60)
    String nickname,
    @NotBlank @Size(min = 8, max = 128) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password
) {
    @Override
    public String toString() {
        return "SignUpReqDto[email=%s, name=%s, nickname=%s, password=****]".formatted(email, name, nickname);
    }
}
