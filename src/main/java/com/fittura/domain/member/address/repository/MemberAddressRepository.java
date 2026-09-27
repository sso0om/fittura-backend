package com.fittura.domain.member.address.repository;

import com.fittura.domain.member.address.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {
}
