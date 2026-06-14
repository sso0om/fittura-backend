package com.fittura.domain.order.cart.controller;

import com.fittura.domain.order.cart.dto.request.CartItemCreateReqDto;
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
}
