package com.fittura.domain.member.address.support;

import com.fittura.domain.member.address.entity.MemberAddress;

public class MemberAddressFixture {

    private MemberAddressFixture() {
    }

    public static MemberAddress address(Long memberId, boolean defaultAddress) {
        return MemberAddress.create(
            memberId,
            "우리집",
            "홍길동",
            "01012341234",
            "12345",
            "서울특별시 중구 서소문로 127",
            "시청역",
            "서울특별시",
            "중구",
            defaultAddress
        );
    }
}
