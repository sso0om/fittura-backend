package com.fittura.domain.member.address.dto.response;

import com.fittura.domain.member.address.entity.MemberAddress;

public record MemberAddressResDto(
    Long addressId,
    String addressName,
    String receiverName,
    String phoneNumber,
    String zipCode,
    String address,
    String addressDetail,
    String sido,
    String sigungu,
    boolean defaultAddress
) {
    public static MemberAddressResDto from(MemberAddress address) {
        return new MemberAddressResDto(
            address.getId(),
            address.getAddressName(),
            address.getReceiverName(),
            address.getPhoneNumber(),
            address.getZipCode(),
            address.getAddress(),
            address.getAddressDetail(),
            address.getSido(),
            address.getSigungu(),
            address.isDefaultAddress()
        );
    }
}
