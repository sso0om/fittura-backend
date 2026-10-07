package com.fittura.domain.order.order.controller;

import com.fittura.domain.order.facade.OrderFacade;
import com.fittura.domain.order.order.dto.request.*;
import com.fittura.domain.order.order.dto.response.OrderPreviewResDto;
import com.fittura.domain.order.order.dto.response.OrderWithAllResDto;
import com.fittura.domain.order.order.dto.response.OrderWithDeliveryResDto;
import com.fittura.global.rsdata.RsData;
import com.fittura.global.security.LogInMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order V1", description = "주문 CRUD 관련 API")
public class OrderController {

    private final OrderFacade orderFacade;

    @GetMapping
    @Operation(summary = "주문 목록 조회", description = "주문 목록 조회 API - 주문 기간(startDate, endDate) 필수")
    public ResponseEntity<RsData<Page<OrderWithDeliveryResDto>>> getAllOrders(
        @LogInMemberId Long memberId,
        @RequestParam(required = false) String orderNumber,
        @RequestParam(required = false) String productName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Schema(example = "2026-01-15") LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Schema(example = "2026-01-15") LocalDate endDate,
        @ParameterObject Pageable pageable
    ) {
        OrderSearchCondition searchCondition = new OrderSearchCondition(
            orderNumber,
            productName,
            startDate,
            endDate
        );
        Page<OrderWithDeliveryResDto> resDtos = orderFacade.getOrders(memberId, searchCondition, pageable);

        return ResponseEntity.ok(RsData.success("주문 목록이 조회되었습니다.", resDtos));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 조회", description = "주문 조회 API")
    public ResponseEntity<RsData<OrderWithAllResDto>> getOrder(
        @LogInMemberId Long memberId,
        @PathVariable Long orderId
    ) {
        OrderWithAllResDto resDto = orderFacade.getOrderByIdAndMember(orderId, memberId);
        return ResponseEntity.ok(RsData.success("주문이 조회되었습니다.", resDto));
    }

    @PostMapping("/preview/cart")
    @Operation(summary = "주문 전 조회 (장바구니)", description = "장바구니에서 넘어온 주문 전 조회 API")
    public ResponseEntity<RsData<OrderPreviewResDto>> getOrderPreviewCart(
        @LogInMemberId Long memberId,
        @RequestBody @Valid CartOrderPreviewReqDto reqDto
        ) {
        OrderPreviewResDto resDto = orderFacade.getOrderPreviewCart(memberId, reqDto);
        return ResponseEntity.ok(RsData.success("주문 정보가 조회되었습니다.", resDto));
    }

    @PostMapping("/preview/direct")
    @Operation(summary = "주문 전 조회(바로 주문)", description = "상품 선택 후 바로 주문으로 넘어온 주문 전 조회 API")
    public ResponseEntity<RsData<OrderPreviewResDto>> getOrderPreviewDirect(
        @LogInMemberId Long memberId,
        @RequestBody @Valid DirectOrderPreviewReqDto reqDto
    ) {
        OrderPreviewResDto resDto =  orderFacade.getOrderPreviewDirect(memberId, reqDto);
        return ResponseEntity.ok(RsData.success("주문 정보가 조회되었습니다.", resDto));
    }

    @PostMapping("/cart")
    @Operation(summary = "장바구니 주문 생성", description = "장바구니에서 넘어온 주문 생성 API")
    public ResponseEntity<RsData<Long>> createOrderCart(
        @LogInMemberId Long memberId,
        @RequestBody @Valid CartOrderCreateReqDto reqDto
    ) {
        Long orderId = orderFacade.createOrderCart(memberId, reqDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("주문이 생성되었습니다.", orderId));
    }

    @PostMapping("/direct")
    @Operation(summary = "바로 주문 생성", description = "상품 선택 후 바로 넘어온 주문 생성 API")
    public ResponseEntity<RsData<Long>> createOrderDirect(
        @LogInMemberId Long memberId,
        @RequestBody @Valid DirectOrderCreateReqDto reqDto
    ) {
        Long orderId = orderFacade.createOrderDirect(memberId, reqDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("주문이 생성되었습니다.", orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "주문 취소 API")
    public ResponseEntity<RsData<Void>> updateOrder(
        @LogInMemberId Long memberId,
        @PathVariable Long orderId,
        @RequestBody @Valid ClaimOrderReqDto reqDto
    ) {
        orderFacade.cancelOrder(memberId, orderId, reqDto);
        return ResponseEntity.ok(RsData.success("주문이 취소되었습니다.", null));
    }
}
