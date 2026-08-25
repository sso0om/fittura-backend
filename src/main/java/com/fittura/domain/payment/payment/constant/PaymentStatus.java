package com.fittura.domain.payment.payment.constant;

public enum PaymentStatus {
    PENDING,                 // 결제 대기
    APPROVED,                // 결제 승인
    FAILED,                  // 결제 실패
    UNKNOWN,                 // 통신 오류
    PARTIALLY_CANCELLED,     // 부분 취소
    FULLY_CANCELLED          // 전체 취소
}
