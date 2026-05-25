package com.fittura.domain.category.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

    // 400
    NOT_SELF_PARENT(HttpStatus.BAD_REQUEST, "CT400-01", "자기 자신을 상위 카테고리로 지정할 수 없습니다."),
    NOT_DESCENDANT_PARENT(HttpStatus.BAD_REQUEST, "CT400-02", "카테고리 구성이 올바르지 않습니다."),
    NOT_ARCHIVED_PARENT(HttpStatus.BAD_REQUEST, "CT400-03", "ARCHIVED 카테고리로 이동할 수 없습니다."),
    PARENT_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "CT400-04", "부모 카테고리가 활성 상태가 아니면 활성화할 수 없습니다."),
    NOT_LEAF_CATEGORY(HttpStatus.BAD_REQUEST, "CT400-05", "하위 카테고리에만 상품을 등록할 수 있습니다."),
    ARCHIVED_CATEGORY(HttpStatus.BAD_REQUEST, "CT400-06", "ARCHIVED 카테고리는 사용불가 합니다."),

    // 404
    NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND,  "CT404-01", "존재하지 않는 카테고리입니다."),
    NOT_FOUND_PARENT_CATEGORY(HttpStatus.NOT_FOUND,  "CT404-02", "존재하지 않는 상위 카테고리입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
