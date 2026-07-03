package com.fittura.domain.order.cart.repository;

import com.fittura.domain.order.cart.dto.response.CartItemResDto;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.fittura.domain.order.cart.entity.QCartItem.cartItem;
import static com.fittura.domain.product.product.entity.QProduct.product;
import static com.fittura.domain.product.sku.entity.QProductSku.productSku;

@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CartItem> findAllWithSkuForUpdate(List<Long> itemIds, Long memberId) {
        return queryFactory
            .selectFrom(cartItem)
            .join(cartItem.productSku, productSku).fetchJoin()
            .join(productSku.product, product).fetchJoin()
            .where(
                cartItem.id.in(itemIds),
                cartItem.cart.memberId.eq(memberId),
                productSku.status.ne(SkuStatus.ARCHIVED),
                product.status.ne(ProductStatus.ARCHIVED)
            )
            .orderBy(cartItem.id.asc())
            .fetch();
    }

    @Override
    public List<CartItemResDto> findCartItemDtosByCart(Long cartId) {
        return queryFactory
            .select(Projections.constructor(CartItemResDto.class,
                cartItem.id,
                product.id,
                product.name,
                productSku.id,
                productSku.color,
                productSku.material,
                productSku.price,
                cartItem.quantity,
                productSku.price.multiply(cartItem.quantity),
                product.status,
                productSku.status
                ))
            .from(cartItem)
            .join(cartItem.productSku, productSku)
            .join(productSku.product, product)
            .where(
                cartItem.cart.id.eq(cartId),
                productSku.status.ne(SkuStatus.ARCHIVED),
                product.status.ne(ProductStatus.ARCHIVED)
            )
            .orderBy(
                new CaseBuilder()
                    .when(productSku.status.eq(SkuStatus.ACTIVE))
                    .then(0)
                    .otherwise(1).asc(),
                cartItem.modifiedDate.desc()
            )
            .fetch();
    }
}
