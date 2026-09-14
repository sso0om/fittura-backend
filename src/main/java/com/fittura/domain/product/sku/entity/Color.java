package com.fittura.domain.product.sku.entity;

import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "colors")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class Color extends BaseEntity {

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    public static Color create(String name) {
        Color color = new Color();
        color.name = name;
        return color;
    }
}
