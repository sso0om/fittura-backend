package com.fittura.domain.member.address.controller;

import com.fittura.domain.member.address.dto.request.MAddressCreateReqDto;
import com.fittura.domain.member.address.dto.response.MemberAddressResDto;
import com.fittura.domain.member.address.service.MemberAddressService;
import com.fittura.global.rsdata.RsData;
import com.fittura.global.security.LogInMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/memberAddress")
@RequiredArgsConstructor
@Tag(name = "Auth V1", description = "사용자 배송지 CRUD 관련 API")
public class MemberAddressController {

    private final MemberAddressService addressService;

    @GetMapping
    @Operation(summary = "나의 배송지 목록 조회", description = "나의 배송지 목록 조회 API")
    public ResponseEntity<RsData<List<MemberAddressResDto>>> getMemberAddresses(
        @LogInMemberId Long memberId
    ) {
        List<MemberAddressResDto> resDtos = addressService.getMemberAddresses(memberId);
        return ResponseEntity.ok(RsData.success("나의 배송지 목록을 조회했습니다.", resDtos));
    }

    @GetMapping("/default")
    @Operation(summary = "나의 기본 배송지 조회", description = "나의 기본 배송지 조회 API")
    public ResponseEntity<RsData<MemberAddressResDto>> getDefaultAddress(
        @LogInMemberId Long memberId
    ) {
        MemberAddressResDto resDto = addressService.getDefaultAddress(memberId);
        return ResponseEntity.ok(RsData.success("나의 기본 배송지를 조회했습니다.", resDto));
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "배송지 조회", description = "배송지 조회 API")
    public ResponseEntity<RsData<MemberAddressResDto>> getAddress(
        @LogInMemberId Long memberId,
        @PathVariable Long addressId
    ) {
        MemberAddressResDto resDto = addressService.getAddress(memberId, addressId);
        return ResponseEntity.ok(RsData.success("배송지를 조회했습니다.", resDto));
    }

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
