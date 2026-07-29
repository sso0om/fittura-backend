package com.fittura.domain.order.order.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimReason {

    // ===== 단순 변심 (고객 귀책) =====
    CHANGE_OF_MIND("단순 변심"),
    FOUND_CHEAPER("다른 상품 구매"),
    DELIVERY_DELAYED("배송 지연"),

    // ===== 상품 문제 (판매자 귀책) =====
    DEFECTIVE("상품 불량/파손"),
    WRONG_DELIVERY("오배송"),
    DIFFERENT_FROM_DESCRIPTION("상품 설명과 다름"),

    // ===== 기타 =====
    ETC("기타");

    private final String description;
}
