package com.fittura.domain.member.address.repository;

import com.fittura.domain.member.address.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {

    Optional<MemberAddress> findByMemberIdAndDefaultAddressTrue(Long memberId);

    boolean existsByMemberId(Long memberId);
}
