package com.fittura.domain.member.dto;

public record TokenDto(
    String accessToken,
    String refreshToken,
    long refreshTokenExpirationTime
) {}
