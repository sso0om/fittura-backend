package com.fittura.domain.payment.payment.repository;

import com.fittura.domain.payment.payment.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
}
