package com.fittura.domain.order.order.constant;

public enum ClaimStatus {
    REQUESTED,    // 접수
    APPROVED,     // 승인 (PG 취소 호출 전)
    COMPLETED,    // 완료 (환불까지 끝남)
    REJECTED      // 거부
}
