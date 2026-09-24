package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.entity.QProductSku;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;

public final class ProductSkuExpressions {

    private ProductSkuExpressions() {}

    public static BooleanExpression hasAvailableStock(QProductSku sku) {
        return sku.stockQuantity.subtract(sku.reservedQuantity).gt(0);
    }

    public static BooleanExpression isPurchasable(QProductSku sku) {
        return sku.status.eq(SkuStatus.ACTIVE)
            .and(hasAvailableStock(sku));
    }

    public static Expression<Boolean> isSoldOut(QProductSku sku) {
        return new CaseBuilder()
            .when(isPurchasable(sku)).then(false)
            .otherwise(true);
    }
}
