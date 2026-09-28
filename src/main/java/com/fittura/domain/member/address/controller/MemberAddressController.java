package com.fittura.domain.member.address.controller;

import com.fittura.domain.member.address.dto.request.MAddressCreateReqDto;
import com.fittura.domain.member.address.service.MemberAddressService;
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
@RequestMapping("/api/v1/memberAddress")
@RequiredArgsConstructor
@Tag(name = "Auth V1", description = "사용자 배송지 CRUD 관련 API")
public class MemberAddressController {

    private final MemberAddressService addressService;

    @PostMapping
    @Operation(summary = "나의 배송지 등록", description = "나의 배송지 등록 API")
    public ResponseEntity<RsData<Long>> createMemberAddress(
        @LogInMemberId Long memberId,
        @RequestBody @Valid MAddressCreateReqDto reqDto
    ) {
        Long addressId = addressService.createAddress(memberId, reqDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RsData.createSuccess("나의 배송지가 저장되었습니다.", addressId));
    }
}
