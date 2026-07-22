package com.fittura.domain.order.order.controller;

import com.fittura.domain.order.facade.OrderFacade;
import com.fittura.domain.order.order.dto.request.OrderCreateReqDto;
import com.fittura.domain.order.order.dto.request.OrderSearchCondition;
import com.fittura.domain.order.order.dto.response.OrderWithAllResDto;
import com.fittura.domain.order.order.dto.response.OrderWithDeliveryResDto;
import com.fittura.global.rsdata.RsData;
import com.fittura.global.security.LogInMemberId;
import io.swagger.v3.oas.annotations.Operation;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API (V1)", description = "주문 CRUD 관련 API")
public class OrderController {

    private final OrderFacade orderFacade;

    @GetMapping
    @Operation(summary = "주문 목록 조회", description = "주문 목록 조회 API - 주문 기간(startDate, endDate) 필수")
    public ResponseEntity<RsData<Page<OrderWithDeliveryResDto>>> getAllOrders(
        @LogInMemberId Long memberId,
        @RequestParam(required = false) String orderNumber,
        @RequestParam(required = false) String productName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
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

    @GetMapping("/{id}")
    @Operation(summary = "주문 조회", description = "주문 조회 API")
    public ResponseEntity<RsData<OrderWithAllResDto>> getOrder(
        @LogInMemberId Long memberId,
        @PathVariable Long id
    ) {
        OrderWithAllResDto resDto = orderFacade.getOrderByIdAndMember(id, memberId);
        return ResponseEntity.ok(RsData.success("주문이 조회되었습니다.", resDto));
    }

    @PostMapping
    @Operation(summary = "주문 생성", description = "주문 생성 API")
    public ResponseEntity<RsData<Long>> createOrder(
        @LogInMemberId Long memberId,
        @RequestBody @Valid OrderCreateReqDto reqDto
    ) {
        Long orderId = orderFacade.createOrder(memberId, reqDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("주문이 생성되었습니다.", orderId));
    }
}
