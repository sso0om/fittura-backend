package com.fittura.domain.product.sku.constant;

public enum SkuStatus {
    ACTIVE,
    PAUSED,        // 일시 중단
    DISCONTINUED,  // 단종 (판매 영구 종료)
    ARCHIVED       // 삭제 (Soft Delete)
}
