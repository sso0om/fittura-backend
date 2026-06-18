package com.fittura.domain.order.order.repository;

import com.fittura.domain.order.order.entity.OrderShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderShippingAddressRepository extends JpaRepository<OrderShippingAddress, Long> {
}
