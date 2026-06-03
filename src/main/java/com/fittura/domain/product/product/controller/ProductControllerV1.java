package com.fittura.domain.product.product.controller;

import com.fittura.domain.product.facade.ProductFacade;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.dto.request.ProductSearchCondition;
import com.fittura.domain.product.product.dto.response.CompositionResDto;
import com.fittura.domain.product.product.dto.response.ProductAttributeResDto;
import com.fittura.domain.product.product.dto.response.ProductResDto;
import com.fittura.domain.product.product.dto.response.ProductWithSkuResDto;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "제품 API (V1)", description = "사용자용 - 제품 조회 관련 API")
public class ProductControllerV1 {

    private final ProductFacade productFacade;

    @GetMapping
    @Operation(summary = "제품 목록 조회", description = "제품 목록 조회 API. sort 예시: name,asc / basePrice,desc / createdDate,desc")
    public ResponseEntity<RsData<Page<ProductResDto>>> getProducts (
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) List<String> colors,
        @RequestParam(required = false) List<String> materials,
        @ParameterObject Pageable pageable
    ) {
        ProductSearchCondition searchCondition = new ProductSearchCondition(
            List.of(ProductStatus.ACTIVE, ProductStatus.DISCONTINUED),
            categoryId,
            keyword,
            colors,
            materials
        );
        Page<ProductResDto> resDtos = productFacade.getProducts(searchCondition, pageable);

        return ResponseEntity
            .ok(RsData.success("제품 목록이 조회되었습니다.", resDtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "제품 상세 조회", description = "제품 상세 조회 API")
    public ResponseEntity<RsData<ProductWithSkuResDto>> getProduct(
        @PathVariable Long id
    ) {
        ProductWithSkuResDto resDto = productFacade.getProductWithSku(id);

        return ResponseEntity
            .ok(RsData.success("제품이 조회되었습니다.", resDto));
    }

    @GetMapping("/{id}/attributes")
    @Operation(summary = "상품 고시 정보 조회", description = "상품 고시 정보 목록 조회 API")
    public ResponseEntity<RsData<List<ProductAttributeResDto>>> getProductAttributes(
        @PathVariable Long id
    ) {
        List<ProductAttributeResDto> resDto = productFacade.getProductAttributes(id);

        return ResponseEntity
            .ok(RsData.success("상품 고시 정보가 조회되었습니다.", resDto));
    }

    @GetMapping("/{id}/compositions")
    @Operation(summary = "상품 구성 정보 조회", description = "완제품 - 상품 구성 목록 조회 API")
    public ResponseEntity<RsData<List<CompositionResDto>>> getProductCompositions(
        @PathVariable Long id
    ) {
        List<CompositionResDto> resDto = productFacade.getProductCompositions(id);

        return ResponseEntity
            .ok(RsData.success("상품 구성 정보가 조회되었습니다.", resDto));
    }
}
