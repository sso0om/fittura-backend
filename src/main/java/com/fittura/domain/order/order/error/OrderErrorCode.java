package com.fittura.domain.order.order.error;

import com.fittura.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    // 400
    QUANTITY_MUST_BE_POSITIVE(HttpStatus.BAD_REQUEST, "OR400-01", "수량은 1개 이상 선택해야 합니다."),
    QUANTITY_EXCEEDED(HttpStatus.BAD_REQUEST, "OR400-02", "수량은 999개 이하 선택해야 합니다."),
    AMOUNT_MUST_BE_POSITIVE(HttpStatus.BAD_REQUEST, "OR400-03", "금액은 0원 이상이어야 합니다."),
    CART_ITEMS_NOT_VALID(HttpStatus.BAD_REQUEST, "OR400-04" , "상품의 상태를 확인해주세요."),
    SKU_MUST_ACTIVE(HttpStatus.BAD_REQUEST, "OR400-05", "판매중인 상품이 아닙니다."),
    PRODUCT_MUST_ACTIVE(HttpStatus.BAD_REQUEST, "OR400-06", "판매중인 상품이 아닙니다."),
    STOCK_NOT_VALID(HttpStatus.BAD_REQUEST, "OR400-07" , "재고가 부족합니다."),
    DATE_RANGE_INVALID(HttpStatus.BAD_REQUEST, "OR400-08" , "주문 일자 조회 범위를 확인해주세요." ),

    // 404
    NOT_FOUND_ORDER(HttpStatus.NOT_FOUND, "OR404-01", "주문을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
