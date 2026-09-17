package com.fittura.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청 DTO")
public record SignInReqDto(
    @Email @NotBlank @Size(min = 6, max = 254)
    String email,
    @NotBlank @Size(min = 8, max = 128) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password
) {
    @Override
    public String toString() {
        return "SignInReqDto[email=%s, password=****]".formatted(email);
    }
}
