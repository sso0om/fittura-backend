package com.fittura.domain.member.dto;

public record SignUpResultDto(
    Long id,
    String email,
    String nickname,
    TokenDto tokenDto
) {
}
