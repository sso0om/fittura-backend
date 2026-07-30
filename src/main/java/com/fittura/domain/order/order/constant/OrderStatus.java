package com.fittura.domain.order.order.constant;

public enum OrderStatus {
    PENDING,            // 결제 대기
    PAID,               // 결제 완료
    PREPARING,          // 상품 준비 중
    COMPLETED,          // 주문 완료 (배송 완료 후)
    CANCELLED,          // 취소 완료
    RETURNED            // 반품 완료
}
