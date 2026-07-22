package com.fittura.domain.delivery.delivery.repository;

import com.fittura.domain.delivery.delivery.entitiy.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
