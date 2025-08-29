package com.fittura.domain.member.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthError implements ErrorCode {

    // 409
    DUPLICATED_EMAIL(HttpStatus.CONFLICT.value(), "A409-01", "이미 사용중인 이메일입니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT.value(), "A409-02", "이미 사용중인 닉네임입니다.");

    private final int status;
    private final String code;
    private final String message;
}
