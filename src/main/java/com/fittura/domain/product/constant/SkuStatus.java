package com.fittura.domain.product.constant;

public enum SkuStatus {
    ACTIVE,
    SOLDOUT,       // 재고 없음 (재입고 가능)
    DISCONTINUED,  // 단종 (판매 영구 종료)
    ARCHIVED       // 삭제 (Soft Delete)
}
