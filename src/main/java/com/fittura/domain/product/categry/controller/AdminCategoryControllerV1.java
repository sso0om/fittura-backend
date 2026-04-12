package com.fittura.domain.product.categry.controller;

import com.fittura.domain.product.categry.dto.response.CategoryDto;
import com.fittura.domain.product.categry.dto.request.CategoryCreateReqDto;
import com.fittura.domain.product.categry.service.CategoryService;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/categories")
@RequiredArgsConstructor
@Tag(name = "관리자용 카테고리 API (V1)", description = "관리자용 - 카테고리 CRUD 관련 API")
public class AdminCategoryControllerV1 {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "카테고리 생성", description = "카테고리 생성 API")
    public ResponseEntity<RsData<CategoryDto>> createCategory(
        @RequestBody @Valid CategoryCreateReqDto reqDto
    ) {
        CategoryDto resDto = categoryService.createCategory(reqDto);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("카테고리가 생성되었습니다.", resDto));
    }
}
