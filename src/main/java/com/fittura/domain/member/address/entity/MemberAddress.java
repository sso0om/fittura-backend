package com.fittura.domain.member.address.entity;

import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "member_addresses",
    indexes = @Index(
        name = "idx_member_addresses_member_id",
        columnList = "member_id"
    )
)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class MemberAddress extends BaseEntity {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 50)
    private String addressName;

    @Column(nullable = false, length = 100)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 5)
    private String zipCode;

    @Column(nullable = false)
    private String address;

    @Column
    private String addressDetail;

    @Column(nullable = false, length = 20)
    private String sido;

    @Column(nullable = false, length = 20)
    private String sigungu;

    @Column(nullable = false)
    private boolean defaultAddress;

    public static MemberAddress create(
        Long memberId,
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
        Objects.requireNonNull(memberId, "memberId must not be null");

        return MemberAddress.builder()
            .memberId(memberId)
            .addressName(addressName)
            .receiverName(receiverName)
            .phoneNumber(phoneNumber)
            .zipCode(zipCode)
            .address(address)
            .addressDetail(addressDetail)
            .sido(sido)
            .sigungu(sigungu)
            .defaultAddress(defaultAddress)
            .build();
    }

    public void update(
        String addressName,
        String receiverName,
        String phoneNumber,
        String zipCode,
        String address,
        String addressDetail,
        String sido,
        String sigungu
    ) {
        this.addressName = addressName;
        this.receiverName = receiverName;
        this.phoneNumber = phoneNumber;
        this.zipCode = zipCode;
        this.address = address;
        this.addressDetail = addressDetail;
        this.sido = sido;
        this.sigungu = sigungu;
    }

    public void markDefault() {
        this.defaultAddress = true;
    }

    public void unmarkDefault() {
        this.defaultAddress = false;
    }
}