package com.fittura.domain.member.dto.response;

import com.fittura.domain.member.dto.AuthResultDto;

public record AuthResDto(
    Long id,
    String email,
    String nickname,
    String accessToken
) {
    public static AuthResDto of(AuthResultDto resultDto) {
        return new AuthResDto(
            resultDto.id(),
            resultDto.email(),
            resultDto.nickname(),
            resultDto.tokenDto().accessToken()
        );
    }

    @Override
    public String toString() {
        return "AuthResDto[id=%d, email=%s, nickname=%s, accessToken=****]".formatted(id, email, nickname);
    }
}
