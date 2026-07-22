package com.fittura.domain.order.order.constant;

public enum OrderItemStatus {
    ORDERED,            // 주문 완료
    CANCEL_REQUESTED,   // 취소 요청
    CANCELLED,          // 취소 완료
    RETURN_REQUESTED,   // 반품 요청
    RETURNED            // 반품 완료
}
