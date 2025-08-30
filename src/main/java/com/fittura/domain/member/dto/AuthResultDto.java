package com.fittura.domain.member.dto;

import com.fittura.domain.member.entity.Member;

public record AuthResultDto(
    Long id,
    String email,
    String nickname,
    TokenDto tokenDto
) {
    public static AuthResultDto of(Member member, TokenDto tokenDto) {
        return new AuthResultDto(
            member.getId(),
            member.getEmail(),
            member.getNickname(),
            tokenDto
        );
    }
}
