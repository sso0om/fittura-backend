package com.fittura.domain.member.address.service;

import com.fittura.domain.member.address.dto.request.MAddressCreateReqDto;
import com.fittura.domain.member.address.dto.request.MAddressUpdateReqDto;
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
    public List<MemberAddressResDto> getAddresses(Long memberId) {
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
        return MemberAddressResDto.from(getAddressByIdAndMember(addressId, memberId));
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

    @Transactional
    public void updateAddress(Long memberId, Long addressId, MAddressUpdateReqDto reqDto) {
        MemberAddress address = getAddressByIdAndMember(addressId, memberId);

        address.update(
            reqDto.addressName(), reqDto.receiverName(), reqDto.phoneNumber(),
            reqDto.zipCode(), reqDto.address(), reqDto.addressDetail(),
            reqDto.sido(), reqDto.sigungu()
        );

        if (reqDto.defaultAddress()) {
            changeDefaultAddress(memberId, address);
        }
    }

    @Transactional
    public void changeDefaultMemberAddress(Long memberId, Long addressId) {
        MemberAddress address = getAddressByIdAndMember(addressId, memberId);
        changeDefaultAddress(memberId, address);
    }

    @Transactional
    public void deleteMemberAddress(Long memberId, Long addressId) {
        MemberAddress address = getAddressByIdAndMember(addressId, memberId);
        if (address.isDefaultAddress() && existsByMemberIdAndIdNot(memberId, addressId)) {
            throw new ServiceException(MemberAddressError.CAN_NOT_DELETE_DEFAULT_ADDRESS);
        }
        addressRepository.delete(address);
    }


    // ========== 헬퍼 메서드 ==========

    private boolean existsByMemberId(Long memberId) {
        return addressRepository.existsByMemberId(memberId);
    }

    private boolean existsByMemberIdAndIdNot(Long memberId, Long addressId) {
        return addressRepository.existsByMemberIdAndIdNot(memberId, addressId);
    }

    private Optional<MemberAddress> getOpDefaultAddress(Long memberId) {
        return addressRepository.findByMemberIdAndDefaultAddressTrue(memberId);
    }

    private MemberAddress getAddressByIdAndMember(Long addressId, Long memberId) {
        return addressRepository.findByIdAndMemberId(addressId, memberId)
            .orElseThrow(() -> new ServiceException(MemberAddressError.NOT_FOUND_ADDRESS));
    }

    private void changeDefaultAddress(Long memberId, MemberAddress address) {
        if (address.isDefaultAddress()) return;

        getOpDefaultAddress(memberId)
            .ifPresent(MemberAddress::unmarkDefault);

        address.markDefault();
    }
}
