package com.fittura.domain.payment.payment.service;

import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.payment.payment.dto.request.PaymentPrepareReqDto;
import com.fittura.domain.payment.payment.dto.response.PaymentPrepareResDto;
import com.fittura.domain.payment.payment.entity.Payment;
import com.fittura.domain.payment.payment.repository.InstitutionCodeRepository;
import com.fittura.domain.payment.payment.repository.PaymentCardRepository;
import com.fittura.domain.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final InstitutionCodeRepository institutionCodeRepository;

    public PaymentPrepareResDto createPayment(Order order, PaymentPrepareReqDto reqDto) {
        order.validatePayable();
        
        Payment payment = Payment.create(
            order.getId(), reqDto.pgProvider(), reqDto.paymentMethod(), order.getFinalAmount());
        paymentRepository.save(payment);

        return PaymentPrepareResDto.from(payment);
    }
}
