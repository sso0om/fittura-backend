package com.fittura.domain.payment.payment.repository;

import com.fittura.domain.payment.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
