package com.fittura.domain.product.product.controller;

import com.fittura.domain.product.facade.ProductFacade;
import com.fittura.domain.product.product.dto.response.ProductResDto;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "제품 API (V1)", description = "사용자용 - 제품 조회 관련 API")
public class ProductControllerV1 {

    private final ProductFacade productFacade;

    @GetMapping("/{id}")
    @Operation(summary = "제품 상세 조회", description = "제품 상세 조회 API")
    public ResponseEntity<RsData<ProductResDto>> getProduct(
        @PathVariable Long id
    ) {
        ProductResDto resDto = productFacade.getProduct(id);

        return ResponseEntity
            .ok(RsData.success("제품이 조회되었습니다.", resDto));
    }
}
