package com.fittura.domain.product.product.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // 400
    COMPLETE_HAVE_COMPOSITIONS(HttpStatus.BAD_REQUEST, "P400-01", "완제품은 구성품을 가지고 있어야 합니다."),
    COMPONENT_NOT_HAVE_COMPOSITION(HttpStatus.BAD_REQUEST, "P400-02", "단품은 구성품을 가질 수 없습니다."),
    COMPOSITION_ONLY_COMPLETE(HttpStatus.BAD_REQUEST, "P400-03", "완제품만 구성품을 구성할 수 있습니다."),
    CHILD_SKU_ONLY_COMPONENT(HttpStatus.BAD_REQUEST, "P400-04", "완제품의 구성품은 단품 SKU만 등록할 수 있습니다."),

    // 404
    NOT_FOUND_PRODUCT(HttpStatus.NOT_FOUND, "P404-01", "존재하지 않는 상품입니다."),
    NOT_FOUND_SKU(HttpStatus.NOT_FOUND, "P404-02", "존재하지 않는 SKU 입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
