package com.fittura.domain.product.sku.constant;

public enum SkuStatus {
    ACTIVE,
    SOLDOUT,       // 재고 없음 (일시품절)
    DISCONTINUED,  // 단종 (판매 영구 종료)
    ARCHIVED       // 삭제 (Soft Delete)
}
