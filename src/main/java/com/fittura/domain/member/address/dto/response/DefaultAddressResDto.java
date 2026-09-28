package com.fittura.domain.member.address.dto.response;

import com.fittura.domain.member.address.entity.MemberAddress;

public record DefaultAddressResDto(
    Long addressId,
    String addressName,
    String zipCode,
    String address,
    String addressDetail
) {
    public static DefaultAddressResDto from(MemberAddress address) {
        return new DefaultAddressResDto(
            address.getId(),
            address.getAddressName(),
            address.getZipCode(),
            address.getAddress(),
            address.getAddressDetail()
        );
    }
}
