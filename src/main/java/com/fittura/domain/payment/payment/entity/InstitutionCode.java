package com.fittura.domain.payment.payment.entity;

import com.fittura.domain.payment.payment.constant.PaymentMethod;
import com.fittura.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "institution_codes")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class InstitutionCode extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 20)
    private String code;


    // ===== 생성 =====

    public static InstitutionCode create(PaymentMethod paymentMethod, String name, String code) {
        return InstitutionCode.builder()
            .paymentMethod(paymentMethod)
            .name(name)
            .code(code)
            .build();
    }
}
