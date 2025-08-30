package com.fittura.domain.member.dto.response;

import com.fittura.domain.member.dto.AuthResultDto;

public record AuthResDto(
    Long id,
    String email,
    String nickname,
    String accessToken
) {
    public static AuthResDto from(AuthResultDto resultDto, String accessToken) {
        return new AuthResDto(
            resultDto.id(),
            resultDto.email(),
            resultDto.nickname(),
            accessToken
        );
    }
}
