package com.fittura.domain.payment.payment.service;

import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.payment.payment.dto.request.PaymentPrepareReqDto;
import com.fittura.domain.payment.payment.dto.response.PaymentPrepareResDto;
import com.fittura.domain.payment.payment.entity.Payment;
import com.fittura.domain.payment.payment.entity.PaymentCard;
import com.fittura.domain.payment.payment.error.PaymentErrorCode;
import com.fittura.domain.payment.payment.repository.InstitutionCodeRepository;
import com.fittura.domain.payment.payment.repository.PaymentCardRepository;
import com.fittura.domain.payment.payment.repository.PaymentRepository;
import com.fittura.domain.payment.pg.PgCardResponse;
import com.fittura.domain.payment.pg.PgPaymentResponse;
import com.fittura.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final InstitutionCodeRepository institutionCodeRepository;

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ServiceException(PaymentErrorCode.NOT_FOUND_PAYMENT));
    }

    public PaymentPrepareResDto createPayment(Order order, PaymentPrepareReqDto reqDto) {
        order.validatePayable();
        
        Payment payment = Payment.create(
            order.getId(), reqDto.pgProvider(), reqDto.paymentMethod(), order.getFinalAmount());
        paymentRepository.save(payment);

        return PaymentPrepareResDto.from(payment);
    }

    public void approvePayment(Payment payment, PgPaymentResponse paymentRes) {
        payment.approve(paymentRes.paymentKey(), paymentRes.approvedDate(), paymentRes.rawResponse());
        PgCardResponse cardRes = paymentRes.card();

        PaymentCard card = PaymentCard.create(
            payment, cardRes.issuerCode(), cardRes.cardNumberMasked(),
            cardRes.installmentMonths(), cardRes.interestFree(), cardRes.approvalNumber());

        paymentCardRepository.save(card);
    }

    public void validatePgResponse(Payment payment, PgPaymentResponse paymentRes) {
        if (!Objects.equals(payment.getPaymentNumber(), paymentRes.paymentNumber()) ||
            !Objects.equals(payment.getTotalAmount(), paymentRes.totalAmount())
        ) {
            throw new ServiceException(PaymentErrorCode.NOT_VALID_PG);
        }
    }
}
