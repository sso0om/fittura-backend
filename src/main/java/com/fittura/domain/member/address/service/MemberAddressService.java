package com.fittura.domain.member.address.service;

import com.fittura.domain.member.address.dto.request.MAddressCreateReqDto;
import com.fittura.domain.member.address.dto.response.MemberAddressResDto;
import com.fittura.domain.member.address.entity.MemberAddress;
import com.fittura.domain.member.address.error.MemberAddressError;
import com.fittura.domain.member.address.repository.MemberAddressRepository;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberAddressService {

    private final MemberAddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<MemberAddressResDto> getMemberAddresses(Long memberId) {
        return addressRepository.findAllByMemberId(memberId).stream()
            .map(MemberAddressResDto::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public MemberAddressResDto getDefaultAddress(Long memberId) {
        return getOpDefaultAddress(memberId)
            .map(MemberAddressResDto::from)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public MemberAddressResDto getAddress(Long memberId, Long addressId) {
        MemberAddress address = addressRepository.findByIdAndMemberId(addressId, memberId)
            .orElseThrow(() -> new ServiceException(MemberAddressError.NOT_FOUND_ADDRESS));

        return MemberAddressResDto.from(address);
    }

    @Transactional
    public Long createAddress(Long memberId, MAddressCreateReqDto reqDto) {
        boolean defaultAddress = reqDto.defaultAddress() || !existsByMemberId(memberId);

        if (reqDto.defaultAddress()) {
            getOpDefaultAddress(memberId)
                .ifPresent(MemberAddress::unmarkDefault);
        }

        MemberAddress address = MemberAddress.create(
            memberId, reqDto.addressName(), reqDto.receiverName(), reqDto.phoneNumber(),
            reqDto.zipCode(), reqDto.address(), reqDto.addressDetail(),
            reqDto.sido(), reqDto.sigungu(), defaultAddress
        );
        addressRepository.save(address);
        return address.getId();
    }


    // ========== 헬퍼 메서드 ==========

    private boolean existsByMemberId(Long memberId) {
        return addressRepository.existsByMemberId(memberId);
    }

    private Optional<MemberAddress> getOpDefaultAddress(Long memberId) {
        return addressRepository.findByMemberIdAndDefaultAddressTrue(memberId);
    }
}
