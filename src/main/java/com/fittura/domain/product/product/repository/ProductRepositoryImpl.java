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
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
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
        BooleanExpression[] conditions = {
            statusIn(condition.includedStatuses()),
            categoryIn(condition.categoryIds()),
            keywordContains(condition.keyword()),
            skuOptionMatches(condition.colors(), condition.materials(), condition.inStockOnly())
        };

        List<ProductResDto> products = queryFactory
            .select(Projections.constructor(ProductResDto.class,
                product.id,
                product.name,
                product.basePrice,
                product.status,
                product.productType,
                product.createdDate,
                isSoldOut(),
                product.mainImage.imageUrl
            ))
            .from(product)
            .leftJoin(product.mainImage)
            .where(conditions)
            .orderBy(getOrderSpecifier(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(product.count())
            .from(product)
            .where(conditions)
            .fetchOne();

        return new PageImpl<>(products, pageable, total == null ? 0L : total);
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
                product.deliveryType,
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
                productSku.color.name,
                productSku.material.name
            ))
            .from(productSku)
            .leftJoin(productSku.color)
            .leftJoin(productSku.material)
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
            productRow.deliveryType(),
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
                product.category.id,
                product.name,
                product.description,
                product.productType,
                product.deliveryType,
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
                productSku.color.name,
                productSku.material.name
            ))
            .from(productSku)
            .leftJoin(productSku.color)
            .leftJoin(productSku.material)
            .where(
                productSku.product.id.eq(id),
                productSku.status.ne(SkuStatus.ARCHIVED)
            )
            .fetch();

        return Optional.of(new ProductWithSkuResDto(
            productRow.id(),
            productRow.categoryId(),
            productRow.name(),
            productRow.description(),
            productRow.productType(),
            productRow.deliveryType(),
            productRow.deliveryFee(),
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

    private BooleanExpression categoryIn(List<Long> categoryIds) {
        if (categoryIds == null) return null;
        if (categoryIds.isEmpty()) return Expressions.FALSE;
        return product.category.id.in(categoryIds);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? product.name.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression skuOptionMatches(List<Long> colorIds, List<Long> materialIds, boolean inStockOnly) {
        boolean hasColor = colorIds != null && !colorIds.isEmpty();
        boolean hasMaterial = materialIds != null && !materialIds.isEmpty();
        if (!hasColor && !hasMaterial && !inStockOnly) return null;

        QProductSku sku = new QProductSku("filterSku");

        BooleanExpression cond = sku.product.id.eq(product.id);
        if (hasColor) cond = cond.and(sku.color.id.in(colorIds));
        if (hasMaterial) cond = cond.and(sku.material.id.in(materialIds));
        if (inStockOnly) {
            cond = cond
                .and(sku.status.eq(SkuStatus.ACTIVE))
                .and(sku.stockQuantity.subtract(sku.reservedQuantity).gt(0));
        }

        return JPAExpressions
            .selectOne()
            .from(sku)
            .where(cond)
            .exists();
    }

    private BooleanExpression isSoldOut() {
        QProductSku subSku = new QProductSku("subSku");

        return JPAExpressions
            .selectOne()
            .from(subSku)
            .where(
                subSku.product.id.eq(product.id),
                subSku.status.eq(SkuStatus.ACTIVE),
                subSku.stockQuantity.subtract(subSku.reservedQuantity).gt(0)
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
