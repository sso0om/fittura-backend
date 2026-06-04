package com.fittura.domain.product.sku.repository;

import com.fittura.domain.product.product.dto.response.CompositionResDto;
import com.fittura.domain.product.product.entity.QProduct;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.fittura.domain.product.sku.entity.QProductComposition.productComposition;
import static com.fittura.domain.product.sku.entity.QProductSku.productSku;

@RequiredArgsConstructor
public class CompositionRepositoryImpl implements CompositionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CompositionResDto> findCompositionDtosByProductId(Long productId) {
        QProduct childProduct = new QProduct("childProduct");

        return queryFactory
            .select(Projections.constructor(CompositionResDto.class,
                productComposition.childSku.id,
                childProduct.name,
                productComposition.quantity,
                productComposition.sortOrder
            ))
            .from(productComposition)
            .join(productComposition.childSku, productSku)
            .join(productSku.product, childProduct)
            .where(productComposition.parentProduct.id.eq(productId))
            .orderBy(productComposition.sortOrder.asc())
            .fetch();
    }

    @Override
    public boolean isSkuReferencedByOther(Long productId) {
        return queryFactory
            .selectOne()
            .from(productComposition)
            .where(
                productComposition.childSku.product.id.eq(productId),
                productComposition.parentProduct.id.ne(productId)
            )
            .fetchFirst() != null;
    }
}
