package com.fittura.domain.product.product.dto.request;

import com.fittura.domain.product.product.constant.DeliveryType;
import com.fittura.domain.product.product.constant.ProductType;
import com.fittura.domain.product.sku.dto.request.CompositionCreateReqDto;
import com.fittura.domain.product.sku.dto.request.SkuCreateReqDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "상품 생성 요청 DTO")
public record ProductCreateReqDto(

    @Schema(example = "1")
    @NotNull @Positive
    Long categoryId,

    @Schema(example = "북유럽 의자 완제품")
    @NotBlank @Size(max = 255)
    String name,

    @Schema(example = "북유럽 스타일의 원목 의자입니다.")
    String description,

    @Schema(example = "COMPLETE")
    @NotNull
    ProductType productType,

    @Schema(example = "PARCEL")
    DeliveryType deliveryType,

    @Schema(example = "40.5")
    @NotNull @Positive
    Double weight,

    @Schema(example = "150")
    @NotNull @Positive
    Double width,

    @Schema(example = "100")
    @NotNull @Positive
    Double height,

    @Schema(example = "50")
    @NotNull @Positive
    Double depth,

    @Valid @NotNull @Size(min = 1)
    List<SkuCreateReqDto> skus,

    @Valid
    List<AttributeCreateReqDto> attributes,

    @Valid
    List<CompositionCreateReqDto> compositions
) {
    public ProductCreateReqDto {
        attributes = attributes != null ? attributes : List.of();
        compositions = compositions != null ? compositions : List.of();
    }
}