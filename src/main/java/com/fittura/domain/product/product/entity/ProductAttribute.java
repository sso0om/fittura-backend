package com.fittura.domain.product.product.entity;

import com.fittura.domain.product.product.constant.AttributeKey;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "product_attributes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_product_attr_product_key",
            columnNames = {"product_id", "attribute_key"}
        )
    }
)
@NoArgsConstructor(access = PROTECTED)
public class ProductAttribute extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttributeKey attributeKey;

    @Column(nullable = false, length = 255)
    private String attributeValue;

    public static ProductAttribute create(Product product, AttributeKey key, String value) {
        Objects.requireNonNull(product, "product must not be null");

        ProductAttribute productAttribute = new ProductAttribute();
        productAttribute.product = product;
        productAttribute.attributeKey = key;
        productAttribute.attributeValue = value;

        product.addAttribute(productAttribute);
        return productAttribute;
    }
}
