package com.fittura.domain.product.product.entity;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = PROTECTED)
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

    @OneToMany(mappedBy = "product")
    private List<ProductSku> productSkus = new ArrayList<>();

    // ===== 생성 =====

    public static Product create(
        Category category,
        String name,
        String description,
        ProductType productType,
        Long basePrice
    ) {
        Objects.requireNonNull(category, "category must not be null");

        Product product = new Product();
        product.name = name;
        product.description = description;
        product.productType = productType;
        product.basePrice = basePrice;
        product.category = category;
        product.status = ProductStatus.DISABLED;

        return product;
    }

    /**
     * ProductSku.create() 내부에서만 호출
     */
    public void addProductSku(ProductSku productSku) {
        productSkus.add(productSku);
    }

    public boolean isComplete() {
        return productType == ProductType.COMPLETE;
    }

    public boolean isArchived() {
        return status == ProductStatus.ARCHIVED;
    }
}
