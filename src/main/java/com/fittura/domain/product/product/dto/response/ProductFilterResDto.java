package com.fittura.domain.product.product.dto.response;

import java.util.List;

public record ProductFilterResDto(
    List<ColorResDto> colors,
    List<MaterialResDto> materials
) {
}
