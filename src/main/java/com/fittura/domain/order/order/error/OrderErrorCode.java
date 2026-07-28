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
    DELIVERY_ORDER_MISMATCH(HttpStatus.BAD_REQUEST, "OR400-09" , "상품과 배송의 주문이 불일치합니다." ),
    COMPLETED_CAN_NOT_CANCEL(HttpStatus.BAD_REQUEST, "OR400-10" , "배송 완료된 상태입니다. 반품 요청만 가능합니다." ),
    ALREADY_CANCEL_REQUESTED(HttpStatus.BAD_REQUEST, "OR400-11" , "중복된 취소 요청입니다." ),
    NOT_VALID_STATUS(HttpStatus.BAD_REQUEST, "OR400-12", "주문 상태를 확인해주세요."),
    NO_CANCELLABLE_ITEM(HttpStatus.BAD_REQUEST, "OR400-13", "취소 가능한 상품이 없습니다."),
    CLAIM_ITEMS_NOT_VALID(HttpStatus.BAD_REQUEST, "OR400-14" , "취소/반품/교환 불가한 상품이 있습니다." ),

    // 404
    NOT_FOUND_ORDER(HttpStatus.NOT_FOUND, "OR404-01", "주문을 찾을 수 없습니다."),;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
