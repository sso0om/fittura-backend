package com.fittura.domain.product.sku.util;

public class PriceCalculator {

    private PriceCalculator() {}

    // 판매가 = 할인가가 있으면 할인가, 없으면 정가
    public static Long salePrice(Long originalPrice, Long discountPrice) {
        return discountPrice == null ? originalPrice : discountPrice;
    }

    public static Long discountRate(Long originalPrice, Long salePrice) {
        if (originalPrice == 0) {
            return 0L;
        }
        return (originalPrice - salePrice) * 100 / originalPrice;
    }
}
