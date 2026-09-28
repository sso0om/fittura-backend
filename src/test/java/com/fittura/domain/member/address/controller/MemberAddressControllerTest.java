package com.fittura.domain.member.address.controller;

import com.fittura.domain.member.address.entity.MemberAddress;
import com.fittura.domain.member.address.repository.MemberAddressRepository;
import com.fittura.domain.member.address.support.MemberAddressFixture;
import com.fittura.global.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberAddressControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberAddressRepository addressRepository;

    private static final String ADDRESS_URL = "/api/v1/memberAddress";

    // ========== 기본 배송지 조회 ==========

    @Test
    @DisplayName("기본 배송지 조회 성공 - 기본 배송지 반환")
    void getDefaultAddressSuccess() throws Exception {
        // given
        Long memberId = 10L;
        addressRepository.save(MemberAddressFixture.address(memberId, false));
        MemberAddress defaultAddress = addressRepository.save(MemberAddressFixture.address(memberId, true));

        // when & then
        mockMvc.perform(get(ADDRESS_URL + "/default")
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("나의 기본 배송지를 조회했습니다."))
            .andExpect(jsonPath("$.data.addressId").value(defaultAddress.getId()))
            .andExpect(jsonPath("$.data.addressName").value("우리집"))
            .andExpect(jsonPath("$.data.zipCode").value("12345"))
            .andExpect(jsonPath("$.data.address").value("서울특별시 중구 서소문로 127"))
            .andExpect(jsonPath("$.data.addressDetail").value("시청역"));
    }

    @Test
    @DisplayName("기본 배송지 조회 성공 - 등록된 배송지 없음: data null")
    void getDefaultAddressSuccess_noAddress() throws Exception {
        // given
        Long memberId = 11L;

        // when & then
        mockMvc.perform(get(ADDRESS_URL + "/default")
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.data").value((Object) null));
    }


    // ========== 배송지 등록 ==========

    @Test
    @DisplayName("배송지 등록 성공 - 첫 주소는 기본 배송지 미선택이어도 기본 배송지로 저장")
    void createAddressSuccess_firstAddressBecomesDefault() throws Exception {
        // given
        Long memberId = 1L;

        // when & then
        mockMvc.perform(post(ADDRESS_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody(false)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("나의 배송지가 저장되었습니다."));

        MemberAddress saved = addressRepository.findByMemberIdAndDefaultAddressTrue(memberId).orElseThrow();
        assertThat(saved.getAddressName()).isEqualTo("회사");
    }

    @Test
    @DisplayName("배송지 등록 성공 - 새 기본 배송지 등록 시 기존 기본 배송지 해제")
    void createAddressSuccess_replaceDefault() throws Exception {
        // given
        Long memberId = 2L;
        MemberAddress oldDefault = addressRepository.save(MemberAddressFixture.address(memberId, true));

        // when & then
        mockMvc.perform(post(ADDRESS_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody(true)))
            .andDo(print())
            .andExpect(status().isCreated());

        MemberAddress newDefault = addressRepository.findByMemberIdAndDefaultAddressTrue(memberId).orElseThrow();
        assertThat(newDefault.getId()).isNotEqualTo(oldDefault.getId());
        assertThat(oldDefault.isDefaultAddress()).isFalse();
    }


    // ========== 헬퍼 메서드 ==========

    private String reqBody(boolean defaultAddress) {
        return """
            {
                "addressName": "회사",
                "receiverName": "홍길동",
                "phoneNumber": "01012341234",
                "zipCode": "12345",
                "address": "서울특별시 중구 서소문로 127",
                "addressDetail": "시청역",
                "sido": "서울특별시",
                "sigungu": "중구",
                "defaultAddress": %b
            }
            """.formatted(defaultAddress);
    }
}
