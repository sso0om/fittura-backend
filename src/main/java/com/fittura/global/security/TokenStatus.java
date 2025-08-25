package com.fittura.global.security;

/**
 * JWT 토큰의 유효성 검사 결과
 */
public enum TokenStatus {
    VALID,          // 유효한 토큰
    EXPIRED,        // 만료된 토큰
    INVALID,        // 서명 오류, 형식 오류 등 유효하지 않은 토큰
    UNSUPPORTED     // 지원되지 않는 형식의 토큰
}