package com.fittura.domain.product.sku.util;

public class PriceCalculator {

    private PriceCalculator() {}

    public static Long effectivePrice(Long price, Long salePrice) {
        return salePrice == null ? price : salePrice;
    }

    public static Long discountRate(Long price, Long salePrice) {
        if (price == 0 || salePrice == null) {
            return 0L;
        }
        return (price - salePrice) * 100 / price;
    }
}
