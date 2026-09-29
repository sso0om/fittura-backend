package com.fittura.domain.member.address.repository;

import com.fittura.domain.member.address.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {

    @Query("""
        SELECT a FROM MemberAddress a
        WHERE a.memberId = :memberId
        ORDER BY a.defaultAddress DESC, a.id DESC
        """)
    List<MemberAddress> findAllByMemberId(@Param("memberId") Long memberId);

    Optional<MemberAddress> findByIdAndMemberId(Long addressId, Long memberId);

    Optional<MemberAddress> findByMemberIdAndDefaultAddressTrue(Long memberId);

    boolean existsByMemberId(Long memberId);

    boolean existsByMemberIdAndIdNot(Long memberId, Long addressId);
}
