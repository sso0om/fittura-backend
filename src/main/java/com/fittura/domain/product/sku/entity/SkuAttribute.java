package com.fittura.domain.product.sku.entity;

import com.fittura.domain.product.sku.constant.AttributeKey;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
    name = "sku_attributes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_sku_attributes_sku_id_attribute_key",
            columnNames = {"sku_id", "attribute_key"}
        )
    }
)
@NoArgsConstructor(access = PROTECTED)
public class SkuAttribute extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSku sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttributeKey attributeKey;

    @Column(nullable = false, length = 255)
    private String attributeValue;

    public static SkuAttribute create(ProductSku sku, AttributeKey key, String value) {
        SkuAttribute skuAttribute = new SkuAttribute();
        skuAttribute.sku = sku;
        skuAttribute.attributeKey = key;
        skuAttribute.attributeValue = value;

        return skuAttribute;
    }
}
