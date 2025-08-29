package com.fittura.domain.member.dto.response;

import com.fittura.domain.member.dto.SignUpResultDto;

public record SignUpResDto(
    Long id,
    String email,
    String nickname,
    String accessToken
) {
    public static SignUpResDto from(SignUpResultDto resultDto, String accessToken) {
        return new SignUpResDto(
            resultDto.id(),
            resultDto.email(),
            resultDto.nickname(),
            accessToken
        );
    }
}
