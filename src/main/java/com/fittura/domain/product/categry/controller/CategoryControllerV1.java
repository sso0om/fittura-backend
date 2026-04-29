package com.fittura.domain.product.categry.controller;

import com.fittura.domain.product.categry.dto.response.CategoryTreeResDto;
import com.fittura.domain.product.categry.service.CategoryService;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "카테고리 API (V1)", description = "카테고리 조회 관련 API")
public class CategoryControllerV1 {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "활성화 카테고리 전체 조회", description = "활성화된 카테고리 조회(트리) API")
    public ResponseEntity<RsData<List<CategoryTreeResDto>>> getActiveCategories() {
        List<CategoryTreeResDto> resDtos = categoryService.getActiveCategories();

        return ResponseEntity
            .ok(RsData.success(resDtos));
    }
}
