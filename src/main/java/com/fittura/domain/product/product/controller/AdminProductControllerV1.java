package com.fittura.domain.product.product.controller;

import com.fittura.domain.product.facade.ProductFacade;
import com.fittura.domain.product.product.constant.ProductStatus;
import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.dto.request.ProductSearchCondition;
import com.fittura.domain.product.product.dto.request.ProductUpdateReqDto;
import com.fittura.domain.product.product.dto.response.ProductWithAllResDto;
import com.fittura.domain.product.product.dto.response.ProductResDto;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/v1/products")
@RequiredArgsConstructor
@Tag(name = "관리자용 제품 API (V1)", description = "관리자용 - 제품 CRUD 관련 API")
public class AdminProductControllerV1 {

    private final ProductFacade productFacade;

    @GetMapping
    @Operation(summary = "제품 목록 조회", description = "관리자용 제품 목록 조회 API - sort 예시: basePrice,desc / createdDate,desc")
    public ResponseEntity<RsData<Page<ProductResDto>>> getProducts (
        @RequestParam(required = false) List<ProductStatus> statuses,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) List<String> colors,
        @RequestParam(required = false) List<String> materials,
        @ParameterObject Pageable pageable
    ) {
        List<ProductStatus> includedStatuses = (statuses == null || statuses.isEmpty())
            ? List.of(ProductStatus.ACTIVE, ProductStatus.DISABLED, ProductStatus.DISCONTINUED)
            : statuses;

        ProductSearchCondition searchCondition = new ProductSearchCondition(
            includedStatuses,
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
    public ResponseEntity<RsData<ProductWithAllResDto>> getProduct(
        @PathVariable Long id
    ) {
        ProductWithAllResDto resDto = productFacade.getProductWithAll(id);

        return ResponseEntity
            .ok(RsData.success("제품이 조회되었습니다.", resDto));
    }

    @PostMapping
    @Operation(summary = "제품 등록", description = "제품 등록 API")
    public ResponseEntity<RsData<Long>> createProduct(
        @RequestBody @Valid ProductCreateReqDto reqDto
    ) {
        Long productId = productFacade.createProduct(reqDto);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("제품이 생성되었습니다.", productId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "제품 수정", description = "제품 수정 API")
    public ResponseEntity<RsData<Void>> updateProduct(
        @PathVariable Long id,
        @RequestBody @Valid ProductUpdateReqDto reqDto
    ) {
        productFacade.updateProduct(id, reqDto);

        return ResponseEntity
            .ok(RsData.success("제품이 수정되었습니다.", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "제품 삭제", description = "제품 삭제 API")
    public ResponseEntity<RsData<Void>> deleteProduct(
        @PathVariable Long id
    ) {
        productFacade.deleteProduct(id);

        return ResponseEntity
            .ok(RsData.success("<UNK> <UNK>.", null));
    }
}
