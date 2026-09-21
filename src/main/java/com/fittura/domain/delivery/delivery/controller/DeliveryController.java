package com.fittura.domain.delivery.delivery.controller;

import com.fittura.domain.delivery.delivery.dto.response.DeliveryPolicyResDto;
import com.fittura.domain.delivery.facade.DeliveryFacade;
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
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Tag(name = "Delivery V1", description = "배송 CRUD 관련 API")
public class DeliveryController {

    private final DeliveryFacade deliveryFacade;

    @GetMapping("/policy")
    @Operation(summary = "배송 정책 조회", description = "배송 타입별 배송비 정책 조회 API")
    public ResponseEntity<RsData<List<DeliveryPolicyResDto>>> getDeliveryPolicy() {
        return ResponseEntity.ok(
            RsData.success("배송 정책이 조회되었습니다.", DeliveryPolicyResDto.all())
        );
    }
}
