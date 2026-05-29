package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.fittura.domain.product.product.entity.QProduct.product;
import static com.fittura.domain.product.product.entity.QProductAttribute.productAttribute;
import static com.fittura.domain.product.sku.entity.QProductSku.productSku;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Product> findWithDetailById(Long id) {
        Product result = queryFactory
            .selectFrom(product)
            .innerJoin(productSku).on(productSku.product.eq(product)).fetchJoin()
            .leftJoin(productAttribute).on(productAttribute.product.eq(product)).fetchJoin()
            .where(
                product.id.eq(id),
                product.status.ne(ProductStatus.ARCHIVED),
                productSku.status.ne(SkuStatus.ARCHIVED)
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
