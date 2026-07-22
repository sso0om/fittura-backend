package com.fittura.domain.order.order.controller;

import com.fittura.domain.order.facade.OrderFacade;
import com.fittura.domain.order.order.dto.request.OrderCreateReqDto;
import com.fittura.global.rsdata.RsData;
import com.fittura.global.security.LogInMemberId;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API (V1)", description = "주문 CRUD 관련 API")
public class OrderController {

    private final OrderFacade orderFacade;

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
