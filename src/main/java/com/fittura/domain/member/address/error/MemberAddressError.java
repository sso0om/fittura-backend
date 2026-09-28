package com.fittura.domain.member.address.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberAddressError implements ErrorCode {

    // 404
    NOT_FOUND_ADDRESS(HttpStatus.NOT_FOUND, "MA404-01", "존재하지 않은 배송지입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
