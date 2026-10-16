package com.fittura.domain.payment.payment.constant;

public enum PaymentStatus {
    PENDING,                 // 결제 대기
    APPROVED,                // 결제 승인
    FAILED,                  // 결제 실패
    UNKNOWN,                 // 결과 불명
    PARTIALLY_CANCELLED,     // 부분 취소
    FULLY_CANCELLED          // 전체 취소
}
