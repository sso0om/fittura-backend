package com.fittura.domain.payment.payment.repository;

import com.fittura.domain.payment.payment.entity.InstitutionCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionCodeRepository extends JpaRepository<InstitutionCode, String> {
}
