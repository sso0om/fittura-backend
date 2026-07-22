package com.fittura.domain.order.order.repository;

import com.fittura.domain.order.order.dto.request.OrderSearchCondition;
import com.fittura.domain.order.order.dto.response.OrderWithAllResDto;
import com.fittura.domain.order.order.dto.response.OrderWithDeliveryResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepositoryCustom {
    Page<OrderWithDeliveryResDto> findOrders(Long memberId, OrderSearchCondition searchCondition, Pageable pageable);
    Optional<OrderWithAllResDto> findWithAllByIdAndMemberId(Long orderId, Long memberId);
}
