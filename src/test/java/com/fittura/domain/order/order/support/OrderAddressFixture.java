package com.fittura.domain.order.order.support;

import com.fittura.domain.order.order.dto.request.AddressCreateReqDto;

public class OrderAddressFixture {

    private OrderAddressFixture() {}

    public static AddressCreateReqDto addressReqDto() {
        return new AddressCreateReqDto(
            "홍길동",
            "01012341234",
            "12345",
            "서울특별시 중구 서소문로 127",
            "시청역",
            "서울특별시",
            "중구",
            null
        );
    }
}