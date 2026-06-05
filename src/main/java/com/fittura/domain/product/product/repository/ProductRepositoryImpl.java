package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.dto.request.ProductSearchCondition;
import com.fittura.domain.product.product.dto.response.*;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.entity.QProduct;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.response.SkuResDto;
import com.fittura.domain.product.sku.dto.response.SkuWithStockResDto;
import com.fittura.domain.product.sku.entity.QProductSku;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.fittura.domain.product.product.entity.QProduct.product;
import static com.fittura.domain.product.product.entity.QProductAttribute.productAttribute;
import static com.fittura.domain.product.sku.entity.QProductComposition.productComposition;
import static com.fittura.domain.product.sku.entity.QProductSku.productSku;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductResDto> findProducts(ProductSearchCondition condition, Pageable pageable) {
        BooleanExpression colorCond = colorIn(condition.colors());
        BooleanExpression materialCond = materialIn(condition.materials());
        boolean skuFilterExists = colorCond != null || materialCond != null;

        BooleanExpression isSoldOut = isSoldOut();

        JPAQuery<ProductResDto> query = queryFactory
            .select(Projections.constructor(ProductResDto.class,
                product.id,
                product.name,
                product.basePrice,
                product.status,
                product.productType,
                product.createdDate,
                isSoldOut
            ))
            .distinct()
            .from(product);

        if (skuFilterExists) {
            query.leftJoin(productSku).on(productSku.product.id.eq(product.id));
        }

        List<ProductResDto> content = query
            .where(
                statusIn(condition.includedStatuses()),
                categoryEq(condition.categoryId()),
                keywordContains(condition.keyword()),
                colorCond,
                materialCond
            )
            .orderBy(getOrderSpecifier(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(product.countDistinct())
            .from(product);

        if (skuFilterExists) {
            countQuery.leftJoin(productSku).on(productSku.product.id.eq(product.id));
        }

        Long total = countQuery
            .where(
                statusIn(condition.includedStatuses()),
                categoryEq(condition.categoryId()),
                keywordContains(condition.keyword()),
                colorCond,
                materialCond
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Optional<ProductWithAllResDto> findWithAllById(Long id) {
        BooleanExpression isSoldOut = isSoldOut();

        ProductWithAllResDto productRow = queryFactory
            .select(Projections.constructor(ProductWithAllResDto.class,
                product.id,
                product.name,
                product.description,
                product.productType,
                product.status,
                product.basePrice,
                product.dimension.weight,
                product.dimension.width,
                product.dimension.height,
                product.dimension.depth,
                isSoldOut
            ))
            .from(product)
            .where(
                product.id.eq(id),
                product.status.ne(ProductStatus.ARCHIVED)
            )
            .fetchOne();

        if (productRow == null) return Optional.empty();

        List<SkuWithStockResDto> skus = queryFactory
            .select(Projections.constructor(SkuWithStockResDto.class,
                productSku.id,
                productSku.price,
                productSku.stockQuantity,
                productSku.reservedQuantity,
                productSku.status,
                productSku.color,
                productSku.material
            ))
            .from(productSku)
            .where(
                productSku.product.id.eq(id),
                productSku.status.ne(SkuStatus.ARCHIVED)
            )
            .fetch();

        List<ProductAttributeResDto> attributes = queryFactory
            .select(Projections.constructor(ProductAttributeResDto.class,
                productAttribute.id,
                productAttribute.attributeKey,
                productAttribute.attributeValue
            ))
            .from(productAttribute)
            .where(productAttribute.product.id.eq(id))
            .fetch();

        QProduct childProduct = new QProduct("childProduct");

        List<CompositionResDto> compositions = queryFactory
            .select(Projections.constructor(CompositionResDto.class,
                productSku.id,
                childProduct.name,
                productComposition.quantity,
                productComposition.sortOrder
            ))
            .from(productComposition)
            .join(productComposition.childSku, productSku)
            .join(productSku.product, childProduct)
            .where(productComposition.parentProduct.id.eq(id))
            .orderBy(productComposition.sortOrder.asc())
            .fetch();

        return Optional.of(new ProductWithAllResDto(
            productRow.id(),
            productRow.name(),
            productRow.description(),
            productRow.productType(),
            productRow.status(),
            productRow.basePrice(),
            productRow.weight(),
            productRow.width(),
            productRow.height(),
            productRow.depth(),
            productRow.isSoldOut(),
            skus,
            attributes,
            compositions
        ));
    }

    @Override
    public Optional<ProductWithSkuResDto> findWithSkuById(Long id) {
        BooleanExpression isSoldOut = isSoldOut();

        ProductWithSkuResDto productRow = queryFactory
            .select(Projections.constructor(ProductWithSkuResDto.class,
                product.id,
                product.name,
                product.description,
                product.productType,
                product.status,
                product.basePrice,
                product.dimension.weight,
                product.dimension.width,
                product.dimension.height,
                product.dimension.depth,
                isSoldOut
            ))
            .from(product)
            .where(
                product.id.eq(id),
                product.status.in(ProductStatus.ACTIVE, ProductStatus.DISCONTINUED)
            )
            .fetchOne();

        if (productRow == null) return Optional.empty();

        List<SkuResDto> skus = queryFactory
            .select(Projections.constructor(SkuResDto.class,
                productSku.id,
                productSku.price,
                productSku.status,
                productSku.color,
                productSku.material
            ))
            .from(productSku)
            .where(
                productSku.product.id.eq(id),
                productSku.status.ne(SkuStatus.ARCHIVED)
            )
            .fetch();

        return Optional.of(new ProductWithSkuResDto(
            productRow.id(),
            productRow.name(),
            productRow.description(),
            productRow.productType(),
            productRow.status(),
            productRow.basePrice(),
            productRow.weight(),
            productRow.width(),
            productRow.height(),
            productRow.depth(),
            productRow.isSoldOut(),
            skus
        ));
    }


    // ========== BooleanExpression ==========

    private BooleanExpression statusIn(List<ProductStatus> statuses) {
        return (statuses == null || statuses.isEmpty()) ? null : product.status.in(statuses);
    }

    private BooleanExpression categoryEq(Long categoryId) {
        return categoryId == null ? null : product.category.id.eq(categoryId);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? product.name.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression colorIn(List<String> colors) {
        return (colors == null || colors.isEmpty())
            ? null
            : productSku.color.in(colors);
    }

    private BooleanExpression materialIn(List<String> materials) {
        return (materials == null || materials.isEmpty())
            ? null
            : productSku.material.in(materials);
    }

    private static BooleanExpression isSoldOut() {
        QProductSku subSku = new QProductSku("subSku");

        return JPAExpressions
            .selectOne()
            .from(subSku)
            .where(
                subSku.product.id.eq(product.id),
                subSku.status.eq(SkuStatus.ACTIVE)
            )
            .notExists();
    }

    // ========== OrderSpecifier ==========

    private OrderSpecifier<?>[] getOrderSpecifier(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return new OrderSpecifier[]{product.createdDate.desc()};
        }

        return pageable.getSort().stream()
            .map(order -> {
                PathBuilder<Product> path = new PathBuilder<>(Product.class, "product");
                return new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    path.get(order.getProperty(), Comparable.class)
                );
            })
            .toArray(OrderSpecifier[]::new);
    }
}
