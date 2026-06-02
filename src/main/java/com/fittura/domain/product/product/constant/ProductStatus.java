package com.fittura.domain.product.product.constant;

public enum ProductStatus {
    ACTIVE,        // 노출 O, 구매 O
    DISABLED,      // 노출 X, 구매 X (일시적 숨김)
    DISCONTINUED,  // 노출 O, 구매 X (단종)
    ARCHIVED       // 노출 X, 구매 X (완전 비활성, Soft Delete)
}
