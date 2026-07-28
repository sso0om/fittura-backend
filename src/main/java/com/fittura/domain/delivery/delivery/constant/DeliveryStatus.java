package com.fittura.domain.delivery.delivery.constant;

public enum DeliveryStatus {
    READY,        // 배송 준비 (생성 직후)
    ASSIGNED,     // 기사 배정 (설치 전용)
    IN_TRANSIT,   // 배송 중
    DELIVERED,    // 배송 완료
    FAILED,       // 배송 실패
    CANCELLED,    // 배송 취소
    RETURNED      // 반송
}
