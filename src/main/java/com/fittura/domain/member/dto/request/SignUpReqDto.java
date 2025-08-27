package com.fittura.domain.member.dto.request;

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
    @NotBlank @Size(min = 8, max = 60)
    String password
) {}
