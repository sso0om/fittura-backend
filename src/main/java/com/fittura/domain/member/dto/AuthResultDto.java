package com.fittura.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fittura.domain.member.entity.Member;

@JsonIgnoreProperties({"tokenDto"})
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
