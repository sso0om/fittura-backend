package com.fittura.domain.member.dto;

import com.fittura.domain.member.entity.Member;

public record SignUpResultDto(
    Member member,
    TokenDto tokenDto
) {
}
