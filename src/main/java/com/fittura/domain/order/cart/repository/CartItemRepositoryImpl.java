package com.fittura.domain.order.cart.repository;

import com.fittura.domain.order.cart.dto.response.CartItemResDto;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
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
        List<Long> skuIds = queryFactory
            .select(cartItem.productSku.id)
            .from(cartItem)
            .where(
                cartItem.id.in(itemIds),
                cartItem.cart.memberId.eq(memberId)
            )
            .fetch();

        if (skuIds.isEmpty()) {
            return List.of();
        }

        List<Long> sortedSkuIds = skuIds.stream().distinct().sorted().toList();

        // 이 시점에 ProductSku를 처음 로딩 → 락 걸린 채로 최신 데이터가 세션에 캐싱됨
        queryFactory
            .selectFrom(productSku)
            .where(productSku.id.in(sortedSkuIds))
            .orderBy(productSku.id.asc())
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetch();

        // productSku는 이미 세션에 락 걸린 최신 상태로 있음 (인스턴스 재사용)
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
            .orderBy(productSku.id.asc())
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
