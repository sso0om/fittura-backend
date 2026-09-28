package com.fittura.domain.member.address.service;

import com.fittura.domain.member.address.dto.request.MAddressCreateReqDto;
import com.fittura.domain.member.address.entity.MemberAddress;
import com.fittura.domain.member.address.repository.MemberAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberAddressService {

    private final MemberAddressRepository addressRepository;

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

    private boolean existsByMemberId(Long memberId) {
        return addressRepository.existsByMemberId(memberId);
    }

    private Optional<MemberAddress> getOpDefaultAddress(Long memberId) {
        return addressRepository.findByMemberIdAndDefaultAddressTrue(memberId);
    }
}
