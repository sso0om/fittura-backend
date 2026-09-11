package com.fittura.domain.product.product.controller;

import com.fittura.domain.product.facade.ProductFacade;
import com.fittura.domain.product.product.dto.request.ProductSearchReqDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product V1", description = "사용자용 - 제품 조회 관련 API")
public class ProductControllerV1 {

    private final ProductFacade productFacade;

    @GetMapping
    @Operation(summary = "제품 목록 조회", description = "제품 목록 조회 API - sort 예시: basePrice,desc / createdDate,desc")
    public ResponseEntity<RsData<Page<ProductResDto>>> getProducts (
        @ParameterObject ProductSearchReqDto reqDto,
        @ParameterObject Pageable pageable
    ) {
        Page<ProductResDto> resDtos = productFacade.getProducts(reqDto, pageable);
        return ResponseEntity
            .ok(RsData.success("제품 목록이 조회되었습니다.", resDtos));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "제품 상세 조회", description = "제품 상세 조회 API")
    public ResponseEntity<RsData<ProductWithSkuResDto>> getProduct(
        @PathVariable Long productId
    ) {
        ProductWithSkuResDto resDto = productFacade.getProductWithSku(productId);

        return ResponseEntity
            .ok(RsData.success("제품이 조회되었습니다.", resDto));
    }

    @GetMapping("/{productId}/attributes")
    @Operation(summary = "상품 고시 정보 조회", description = "상품 고시 정보 목록 조회 API")
    public ResponseEntity<RsData<List<ProductAttributeResDto>>> getProductAttributes(
        @PathVariable Long productId
    ) {
        List<ProductAttributeResDto> resDto = productFacade.getProductAttributes(productId);

        return ResponseEntity
            .ok(RsData.success("상품 고시 정보가 조회되었습니다.", resDto));
    }

    @GetMapping("/{productId}/compositions")
    @Operation(summary = "상품 구성 정보 조회", description = "완제품 - 상품 구성 목록 조회 API")
    public ResponseEntity<RsData<List<CompositionResDto>>> getProductCompositions(
        @PathVariable Long productId
    ) {
        List<CompositionResDto> resDto = productFacade.getProductCompositions(productId);

        return ResponseEntity
            .ok(RsData.success("상품 구성 정보가 조회되었습니다.", resDto));
    }
}
