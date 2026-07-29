package com.fittura.domain.order.order.repository;

import com.fittura.domain.order.order.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
}
