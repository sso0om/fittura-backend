package com.fittura.domain.product.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Embeddable
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class Dimension {

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double width;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double depth;

    public static Dimension of(Double weight, Double width, Double height, Double depth) {
        if (weight == null || width == null || height == null || depth == null) {
            throw new IllegalArgumentException("weight, width, height, depth must not be null");
        }
        if (weight <= 0 || width <= 0 || height <= 0 || depth <= 0) {
            throw new IllegalArgumentException("weight, width, height, depth must be greater than 0");
        }
        return new Dimension(weight, width, height, depth);
    }
}
