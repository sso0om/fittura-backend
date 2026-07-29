package com.fittura.domain.order.order.repository;

import com.fittura.domain.order.order.entity.ClaimItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimItemRepository extends JpaRepository<ClaimItem, Long> {
}
