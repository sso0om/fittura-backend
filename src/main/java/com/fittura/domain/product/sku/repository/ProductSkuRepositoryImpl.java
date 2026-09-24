package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.response.SkuResDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.fittura.domain.product.sku.entity.QProductSku.productSku;

@RequiredArgsConstructor
public class ProductSkuRepositoryImpl implements ProductSkuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SkuResDto> findSkuDtosByProductId(Long productId) {
        return queryFactory
            .select(Projections.constructor(SkuResDto.class,
                productSku.id,
                productSku.price,
                productSku.salePrice,
                productSku.status,
                productSku.color.name,
                productSku.material.name,
                ProductSkuExpressions.isSoldOut(productSku)
            ))
            .from(productSku)
            .leftJoin(productSku.color)
            .leftJoin(productSku.material)
            .where(
                productSku.product.id.eq(productId),
                productSku.status.ne(SkuStatus.ARCHIVED)
            )
            .fetch();
    }
}
