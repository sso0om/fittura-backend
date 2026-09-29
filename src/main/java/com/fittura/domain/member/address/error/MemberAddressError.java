package com.fittura.domain.member.address.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberAddressError implements ErrorCode {

    // 400
    CAN_NOT_DELETE_DEFAULT_ADDRESS(HttpStatus.BAD_REQUEST, "MA400-01", "기본 배송지는 삭제할 수 없습니다. 다른 배송지를 기본으로 지정한 뒤 삭제해 주세요."),

    // 404
    NOT_FOUND_ADDRESS(HttpStatus.NOT_FOUND, "MA404-01", "존재하지 않은 배송지입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
