package com.fittura.domain.product.product.entity;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.product.error.ProductErrorCode;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.global.exception.ServiceException;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(nullable = false)
    private Long basePrice = 0L;

    @Embedded
    private Dimension dimension;

    @Builder.Default
    @OneToMany(mappedBy = "product")
    private List<ProductSku> productSkus = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    private List<ProductAttribute> attributes = new ArrayList<>();

    // ===== 생성 =====

    public static Product create(
        Category category,
        String name,
        String description,
        ProductType productType,
        Dimension dimension
    ) {
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(dimension, "dimension must not be null");

        return Product.builder()
            .category(category)
            .name(name)
            .description(description)
            .productType(productType)
            .basePrice(0L)
            .dimension(dimension)
            .status(ProductStatus.DISABLED)
            .build();
    }

    /**
     * ProductSku.create() 내부에서만 호출
     */
    public void addProductSku(ProductSku productSku) {
        productSkus.add(productSku);
    }

    public void addAttribute(ProductAttribute productAttribute) {
        attributes.add(productAttribute);
    }


    // ===== 수정 =====

    public void update(
        Category category,
        String name,
        String description,
        Dimension dimension
    ) {
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(dimension, "dimension must not be null");

        this.category = category;
        this.name = name;
        this.description = description;
        this.dimension = dimension;
    }

    public void syncBasePrice() {
        this.basePrice = productSkus.stream()
            .filter(s -> !s.isArchived())
            .mapToLong(ProductSku::getPrice)
            .min()
            .orElseThrow(() -> new ServiceException(ProductErrorCode.PRODUCT_HAVA_SKU));
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }

    public void archive() {
        this.status = ProductStatus.ARCHIVED;
    }


    // ===== 필드 확인 =====

    public boolean isComplete() {
        return productType == ProductType.COMPLETE;
    }

    public boolean isArchived() {
        return status == ProductStatus.ARCHIVED;
    }
}
