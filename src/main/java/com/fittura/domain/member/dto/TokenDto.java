package com.fittura.domain.member.dto;

public record TokenDto(
    String accessToken,
    String refreshToken,
    long refreshTokenExpiresInMillis
) {
    @Override
    public String toString() {
        return "TokenDto[accessToken=****, refreshToken=****, refreshTokenExpiresInMillis=%d]"
            .formatted(refreshTokenExpiresInMillis);
    }
}
