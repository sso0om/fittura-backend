package com.fittura.domain.product.categry.controller;

import com.fittura.domain.product.categry.dto.request.CategoryCreateReqDto;
import com.fittura.domain.product.categry.dto.request.CategoryUpdateReqDto;
import com.fittura.domain.product.categry.dto.response.CategoryResDto;
import com.fittura.domain.product.categry.dto.response.CategoryTreeResDto;
import com.fittura.domain.product.categry.service.CategoryService;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/v1/categories")
@RequiredArgsConstructor
@Tag(name = "관리자용 카테고리 API (V1)", description = "관리자용 - 카테고리 CRUD 관련 API")
public class AdminCategoryControllerV1 {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "카테고리 전체 조회", description = "모든 카테고리 조회(트리) API ")
    public ResponseEntity<RsData<List<CategoryTreeResDto>>> getAllCategories() {
        List<CategoryTreeResDto> resDtos = categoryService.getAllCategories();

        return ResponseEntity
            .ok(RsData.success(resDtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "카테고리 단건 조회", description = "카테고리 단건 조회 API")
    public ResponseEntity<RsData<CategoryResDto>> getCategory(
        @PathVariable Long id
    ) {
        CategoryResDto resDto = categoryService.getCategoryById(id);

        return ResponseEntity
            .ok(RsData.success(resDto));
    }

    @PostMapping
    @Operation(summary = "카테고리 생성", description = "카테고리 생성 API")
    public ResponseEntity<RsData<CategoryResDto>> createCategory(
        @RequestBody @Valid CategoryCreateReqDto reqDto
    ) {
        CategoryResDto resDto = categoryService.createCategory(reqDto);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("카테고리가 생성되었습니다.", resDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "카테고리 단건 수정", description = "카테고리 단건 수정 API")
    public ResponseEntity<RsData<Void>> updateCategory(
        @PathVariable Long id,
        @RequestBody @Valid CategoryUpdateReqDto reqDto
    ) {
        categoryService.updateCategory(id, reqDto);

        return ResponseEntity
            .ok(RsData.success("카테고리가 수정되었습니다.", null));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "카테고리 활성화", description = "카테고리 활성화 API")
    public ResponseEntity<RsData<CategoryResDto>> activateCategory(
        @PathVariable Long id
    ) {
        CategoryResDto resDto = categoryService.activeCategory(id);

        return ResponseEntity
            .ok(RsData.createSuccess("카테고리가 활성화되었습니다.", resDto));
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "카테고리 비활성화", description = "카테고리 비활성화 API")
    public ResponseEntity<RsData<CategoryResDto>> disableCategory(
        @PathVariable Long id
    ) {
        CategoryResDto resDto = categoryService.disableCategory(id);

        return ResponseEntity
            .ok(RsData.createSuccess("카테고리가 비활성화되었습니다.", resDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "카테고리 삭제", description = "카테고리 삭제(archived) API")
    public ResponseEntity<RsData<Void>> deleteCategory(
        @PathVariable Long id
    ) {
        categoryService.deleteCategory(id);

        return ResponseEntity
            .ok(RsData.success("카테고리를 삭제하였습니다.", null));
    }
}
