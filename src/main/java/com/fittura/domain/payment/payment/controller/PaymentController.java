package com.fittura.domain.payment.payment.controller;

import com.fittura.domain.payment.facde.PaymentFacade;
import com.fittura.domain.payment.payment.dto.request.PaymentApproveReqDto;
import com.fittura.domain.payment.payment.dto.request.PaymentPrepareReqDto;
import com.fittura.domain.payment.payment.dto.response.PaymentPrepareResDto;
import com.fittura.global.rsdata.RsData;
import com.fittura.global.security.LogInMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "결제 API (V1)", description = "결제 관련 API")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping
    @Operation(summary = "결제 준비", description = "결제 준비 API")
    public ResponseEntity<RsData<PaymentPrepareResDto>> preparePayment(
        @LogInMemberId Long memberId,
        @RequestBody PaymentPrepareReqDto reqDto
    ) {
        PaymentPrepareResDto resDto = paymentFacade.preparePayment(memberId, reqDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("결제가 준비되었습니다.", resDto));
    }

    @PostMapping("/{paymentId}")
    @Operation(summary = "결제 승인", description = "결제 승인 API")
    public ResponseEntity<RsData<Long>> approvePayment(
        @LogInMemberId Long memberId,
        @PathVariable Long paymentId,
        @RequestBody PaymentApproveReqDto reqDto
    ) {
        Long orderId = paymentFacade.approvePayment(memberId, paymentId, reqDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("결제가 완료되었습니다.", orderId));
    }
}