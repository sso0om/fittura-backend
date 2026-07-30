package com.fittura.domain.order.order.repository;

import com.fittura.domain.order.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {
    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.items oi
        WHERE o.id = :orderId
        AND o.memberId = :memberId
        """)
    Optional<Order> findByIdAndMemberId(@Param("orderId") Long orderId, @Param("memberId") Long memberId);
}
