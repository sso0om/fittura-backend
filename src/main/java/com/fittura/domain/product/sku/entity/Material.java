package com.fittura.domain.product.sku.entity;

import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "materials")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class Material extends BaseEntity {

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    public static Material create(String name) {
        Material material = new Material();
        material.name = name;
        return material;
    }
}
