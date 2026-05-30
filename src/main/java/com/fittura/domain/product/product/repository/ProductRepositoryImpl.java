package com.fittura.domain.product.product.repository;

import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.dto.response.ProductResDto;
import com.fittura.domain.product.product.dto.response.ProductWithStockResDto;
import com.fittura.domain.product.sku.constant.SkuStatus;
import com.fittura.domain.product.sku.dto.responseDto.SkuResDto;
import com.fittura.domain.product.sku.dto.responseDto.SkuWithStockResDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.fittura.domain.product.product.entity.QProduct.product;
import static com.fittura.domain.product.sku.entity.QProductSku.productSku;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProductWithStockResDto> findWithStockById(Long id) {

        ProductWithStockResDto productRow = queryFactory
            .select(Projections.constructor(ProductWithStockResDto.class,
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

        return Optional.of(new ProductWithStockResDto(
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

    @Override
    public Optional<ProductResDto> findWithDetailById(Long id) {

        ProductResDto productRow = queryFactory
            .select(Projections.constructor(ProductResDto.class,
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

        return Optional.of(new ProductResDto(
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
}
