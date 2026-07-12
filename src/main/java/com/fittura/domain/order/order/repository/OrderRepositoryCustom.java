package com.fittura.domain.order.order.repository;

import com.fittura.domain.order.order.dto.response.OrderWithAllResDto;

import java.util.Optional;

public interface OrderRepositoryCustom {
    Optional<OrderWithAllResDto> findWithAllByIdAndMemberId(Long orderId, Long memberId);
}
