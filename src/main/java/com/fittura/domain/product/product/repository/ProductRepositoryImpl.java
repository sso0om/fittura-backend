package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.dto.request.ProductSearchCondition;
import com.fittura.domain.product.product.dto.response.*;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.entity.QProduct;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.response.SkuResDto;
import com.fittura.domain.product.sku.dto.response.SkuWithStockResDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
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
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(product.status.in(condition.includedStatuses()));

        if (condition.categoryId() != null) {
            builder.and(product.category.id.eq(condition.categoryId()));
        }
        if (StringUtils.hasText(condition.keyword())) {
            builder.and(product.name.containsIgnoreCase(condition.keyword()));
        }

        List<ProductResDto> content = queryFactory
            .select(Projections.constructor(ProductResDto.class,
                product.id,
                product.name,
                product.basePrice,
                product.status,
                product.productType
            ))
            .from(product)
            .where(builder)
            .orderBy(getOrderSpecifier(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(product.count())
            .from(product)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Optional<ProductWithAllResDto> findWithAllById(Long id) {

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
                product.dimension.depth
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
            skus,
            attributes,
            compositions
        ));
    }

    @Override
    public Optional<ProductWithSkuResDto> findWithSkuById(Long id) {

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
                product.dimension.depth
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
            skus
        ));
    }

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
