package com.fittura.domain.member.dto.response;

import com.fittura.domain.member.entity.Member;

public record SignUpResDto(
    Long id,
    String email,
    String nickname,
    String accessToken
) {
    public static SignUpResDto from(Member member, String accessToken) {
        return new SignUpResDto(
            member.getId(),
            member.getEmail(),
            member.getNickname(),
            accessToken
        );
    }
}
