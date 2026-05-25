package com.fittura.domain.product.product.controller;

import com.fittura.domain.product.facade.ProductFacade;
import com.fittura.domain.product.product.dto.request.ProductCreateReqDto;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/v1/products")
@RequiredArgsConstructor
@Tag(name = "관리자용 제품 API (V1)", description = "관리자용 - 제품 CRUD 관련 API")
public class AdminProductControllerV1 {

    private final ProductFacade productFacade;

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
