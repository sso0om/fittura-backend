package com.fittura.domain.product.sku;

import com.fittura.domain.product.facade.ProductFacade;
import com.fittura.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/v1/products/{productId}/skus")
@RequiredArgsConstructor
@Tag(name = "관리자용 제품의 SKU API (V1)", description = "관리자용 - 제품의 SKU 상태 관련 API")
public class AdminSkuControllerV1 {

    private final ProductFacade productFacade;

    @PatchMapping("/{skuId}/soldout")
    @Operation(summary = "SKU 일시품절", description = "SKU 일시품절 직접 처리")
    public ResponseEntity<RsData<Void>> soldOutSku(
        @PathVariable Long productId,
        @PathVariable Long skuId
    ) {
        productFacade.soldOutSku(productId, skuId);

        return ResponseEntity.ok(RsData.success("SKU가 일시품절되었습니다.", null));
    }

    @PatchMapping("/{skuId}/discontinue")
    @Operation(summary = "SKU 단종", description = "SKU 단종 처리")
    public ResponseEntity<RsData<Void>> discontinueSku(
        @PathVariable Long productId,
        @PathVariable Long skuId
    ) {
        productFacade.discontinueSku(productId, skuId);

        return ResponseEntity.ok(RsData.success("SKU가 단종되었습니다.", null));
    }
}
