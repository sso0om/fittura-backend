package com.fittura.domain.order.cart.controller;

import com.fittura.domain.order.cart.dto.request.CartItemCreateReqDto;
import com.fittura.domain.order.cart.dto.request.CartItemUpdateReqDto;
import com.fittura.domain.order.cart.dto.response.CartResDto;
import com.fittura.domain.order.facade.CartFacade;
import com.fittura.global.rsdata.RsData;
import com.fittura.global.security.LogInMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "장바구니 API (V1)", description = "장바구니 CRUD 관련 API")
public class CartControllerV1 {

    private final CartFacade cartFacade;

    @GetMapping
    @Operation(summary = "장바구니 조회", description = "장바구니에 담긴 제품 목록 조회 API")
    public ResponseEntity<RsData<CartResDto>> getCart(
        @LogInMemberId Long memberId
    ) {
        CartResDto resDto = cartFacade.getCart(memberId);

        return ResponseEntity.ok(RsData.success("장바구니가 조회되었습니다.", resDto));
    }

    @PostMapping("/items")
    @Operation(summary = "장바구니 담기", description = "장바구니 제품 담기 API")
    public ResponseEntity<RsData<Void>> createCartItem(
        @LogInMemberId Long memberId,
        @RequestBody CartItemCreateReqDto reqDto
    ) {
        cartFacade.createCartItem(memberId, reqDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("제품이 장바구니에 담겼습니다."));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "장바구니 제품 수량 수정", description = "장바구니 제품 수량 수정 API")
    public ResponseEntity<RsData<Void>> updateCartItem(
        @LogInMemberId Long memberId,
        @PathVariable Long itemId,
        @RequestBody CartItemUpdateReqDto reqDto
    ) {
        cartFacade.updateCartItem(memberId, itemId, reqDto);
        return ResponseEntity
            .ok(RsData.success("제품의 수량이 수정되었습니다.", null));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "장바구니 제품 삭제", description = "장바구니 제품 삭제 API")
    public ResponseEntity<RsData<Void>> deleteCartItem(
        @LogInMemberId Long memberId,
        @PathVariable Long itemId
    ) {
        cartFacade.deleteCartItem(memberId, itemId);
        return ResponseEntity
            .ok(RsData.success("장바구니에서 제품을 삭제하였습니다.", null));
    }
}
