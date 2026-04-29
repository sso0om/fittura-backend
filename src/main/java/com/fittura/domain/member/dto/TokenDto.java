package com.fittura.domain.member.dto;

public record TokenDto(
    String accessToken,
    String refreshToken
) {
    @Override
    public String toString() {
        return "TokenDto[accessToken=****, refreshToken=****]";
    }
}
