package com.fittura.domain.member.address.controller;

import com.fittura.domain.member.address.entity.MemberAddress;
import com.fittura.domain.member.address.error.MemberAddressError;
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

    // ========== 배송지 목록 조회 ==========

    @Test
    @DisplayName("배송지 목록 조회 성공 - 내 배송지만 기본 배송지 → 최근 등록 순으로 반환")
    void getMemberAddressesSuccess() throws Exception {
        // given
        Long memberId = 30L;
        Long otherMemberId = 31L;
        MemberAddress oldest = addressRepository.save(MemberAddressFixture.address(memberId, false));
        MemberAddress defaultAddress = addressRepository.save(MemberAddressFixture.address(memberId, true));
        MemberAddress newest = addressRepository.save(MemberAddressFixture.address(memberId, false));
        addressRepository.save(MemberAddressFixture.address(otherMemberId, true));

        // when & then
        mockMvc.perform(get(ADDRESS_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("나의 배송지 목록을 조회했습니다."))
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].addressId").value(defaultAddress.getId()))
            .andExpect(jsonPath("$.data[0].defaultAddress").value(true))
            .andExpect(jsonPath("$.data[1].addressId").value(newest.getId()))
            .andExpect(jsonPath("$.data[2].addressId").value(oldest.getId()));
    }

    @Test
    @DisplayName("배송지 목록 조회 성공 - 등록된 배송지 없음: 빈 목록")
    void getMemberAddressesSuccess_empty() throws Exception {
        // given
        Long memberId = 32L;

        // when & then
        mockMvc.perform(get(ADDRESS_URL)
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }

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


    // ========== 배송지 단건 조회 ==========

    @Test
    @DisplayName("배송지 조회 성공 - 기본 배송지가 아닌 주소도 조회")
    void getAddressSuccess() throws Exception {
        // given
        Long memberId = 20L;
        addressRepository.save(MemberAddressFixture.address(memberId, true));
        MemberAddress address = addressRepository.save(MemberAddressFixture.address(memberId, false));

        // when & then
        mockMvc.perform(get(ADDRESS_URL + "/{addressId}", address.getId())
                .header("Authorization", userBearerToken(memberId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200-01"))
            .andExpect(jsonPath("$.message").value("배송지를 조회했습니다."))
            .andExpect(jsonPath("$.data.addressId").value(address.getId()))
            .andExpect(jsonPath("$.data.zipCode").value("12345"));
    }

    @Test
    @DisplayName("배송지 조회 실패 - 다른 회원의 배송지는 404")
    void getAddressFail_otherMember() throws Exception {
        // given
        Long ownerId = 21L;
        Long otherMemberId = 22L;
        MemberAddress address = addressRepository.save(MemberAddressFixture.address(ownerId, true));

        // when & then
        mockMvc.perform(get(ADDRESS_URL + "/{addressId}", address.getId())
                .header("Authorization", userBearerToken(otherMemberId)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(MemberAddressError.NOT_FOUND_ADDRESS.getCode()));
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
