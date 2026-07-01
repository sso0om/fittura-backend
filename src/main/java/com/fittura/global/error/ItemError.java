package com.fittura.global.error;

public record ItemError(String target, String code, String message) {
    // 검증 실패 항목과 실패 사유를 묶어 ItemError 생성
    public static ItemError of(String target, ErrorCode errorCode) {
        return new ItemError(target, errorCode.getCode(), errorCode.getMessage());
    }
}
