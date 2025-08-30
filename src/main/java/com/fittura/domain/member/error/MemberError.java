package com.fittura.domain.member.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberError implements ErrorCode {

    // 401
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED.value(), "A401-01", "이메일 또는 비밀번호를 확인해 주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED.value(), "A401-02", "유효하지 않은 토큰입니다."),

    // 404
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND.value(),  "A404-01", "존재하지 않는 회원입니다."),

    // 409
    DUPLICATED_EMAIL(HttpStatus.CONFLICT.value(), "A409-01", "이미 사용중인 이메일입니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT.value(), "A409-02", "이미 사용중인 닉네임입니다.");

    private final int status;
    private final String code;
    private final String message;
}
