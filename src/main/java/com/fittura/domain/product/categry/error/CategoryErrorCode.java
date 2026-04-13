package com.fittura.domain.product.categry.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

    // 404
    NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND,  "C404-01", "존재하지 않는 카테고리입니다."),
    NOT_FOUND_PARENT_CATEGORY(HttpStatus.NOT_FOUND,  "C404-02", "존재하지 않는 상위 카테고리입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
