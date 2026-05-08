package com.fittura.domain.category.constant;

public enum CategoryStatus {
    ACTIVE,    // 활성화 (사용자에게 노출)
    DISABLED,  // 비활성화 (일시적으로 숨김)
    ARCHIVED   // 삭제 (데이터는 남기지만 사용하지 않는 상태)
}
