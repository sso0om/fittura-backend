package com.fittura.domain.product.product.controller;

import com.fittura.domain.product.facade.ProductFacade;
import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.domain.product.product.dto.response.ProductWithStockResDto;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/v1/products")
@RequiredArgsConstructor
@Tag(name = "관리자용 제품 API (V1)", description = "관리자용 - 제품 CRUD 관련 API")
public class AdminProductControllerV1 {

    private final ProductFacade productFacade;

    @GetMapping("/{id}")
    @Operation(summary = "제품 상세 조회", description = "제품 상세 조회 API")
    public ResponseEntity<RsData<ProductWithStockResDto>> getProduct(
        @PathVariable Long id
    ) {
        ProductWithStockResDto resDto = productFacade.getProductWithStock(id);

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
}
