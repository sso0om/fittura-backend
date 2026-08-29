package com.fittura.domain.payment.payment.service;

import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.support.OrderFixture;
import com.fittura.domain.payment.payment.constant.PaymentMethod;
import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.constant.PgProvider;
import com.fittura.domain.payment.payment.dto.request.PaymentPrepareReqDto;
import com.fittura.domain.payment.payment.dto.response.PaymentPrepareResDto;
import com.fittura.domain.payment.payment.entity.Payment;
import com.fittura.domain.payment.payment.entity.PaymentCard;
import com.fittura.domain.payment.payment.error.PaymentErrorCode;
import com.fittura.domain.payment.payment.repository.InstitutionCodeRepository;
import com.fittura.domain.payment.payment.repository.PaymentCardRepository;
import com.fittura.domain.payment.payment.repository.PaymentRepository;
import com.fittura.domain.payment.payment.support.PaymentFixture;
import com.fittura.domain.payment.pg.PgCardResponse;
import com.fittura.domain.payment.pg.PgPaymentResponse;
import com.fittura.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentCardRepository paymentCardRepository;
    @Mock private InstitutionCodeRepository institutionCodeRepository;

    @InjectMocks private PaymentService paymentService;

    // ========== 결제 생성 ==========

    @Test
    @DisplayName("결제 생성 성공")
    void createPaymentSuccess() {
        // given
        Order order = OrderFixture.orderWithId(1L, 1L);
        PaymentPrepareReqDto reqDto = new PaymentPrepareReqDto(order.getId(), PgProvider.TOSS, PaymentMethod.CARD);

        // when
        PaymentPrepareResDto result = paymentService.createPayment(order, reqDto);

        // then
        assertThat(result.orderId()).isEqualTo(order.getId());
        assertThat(result.pgProvider()).isEqualTo(PgProvider.TOSS);
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(result.totalAmount()).isEqualTo(order.getFinalAmount());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 생성 실패 - 결제 가능한 주문 상태가 아님")
    void createPaymentFail_orderNotPayable() {
        // given
        Order order = OrderFixture.orderWithId(1L, 1L);
        order.prepare();
        PaymentPrepareReqDto reqDto = new PaymentPrepareReqDto(order.getId(), PgProvider.TOSS, PaymentMethod.CARD);

        // when & then
        assertThatThrownBy(() -> paymentService.createPayment(order, reqDto))
            .isInstanceOf(ServiceException.class)
            .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                .isEqualTo(OrderErrorCode.NOT_PAYABLE_STATUS));

        verify(paymentRepository, never()).save(any(Payment.class));
    }


    // ========== 결제 승인 ==========

    @Test
    @DisplayName("결제 승인 성공")
    void approvePaymentSuccess() {
        // given
        Payment payment = PaymentFixture.payment(1L);
        PgPaymentResponse paymentRes = pgPaymentResponse(payment.getPaymentNumber(), 10000L);

        // when
        paymentService.approvePayment(payment, paymentRes);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getPgTransactionId()).isEqualTo(paymentRes.paymentKey());
        assertThat(payment.getRawResponse()).isEqualTo(paymentRes.rawResponse());
        verify(paymentCardRepository).save(any(PaymentCard.class));
    }


    // ========== PG 응답 검증 ==========

    @Test
    @DisplayName("PG 응답 검증 성공")
    void validatePgResponseSuccess() {
        // given
        Payment payment = PaymentFixture.payment(1L);
        PgPaymentResponse paymentRes = pgPaymentResponse(payment.getPaymentNumber(), 10000L);

        // when & then (no exception)
        paymentService.validatePgResponse(payment, paymentRes);
    }

    @Test
    @DisplayName("PG 응답 검증 실패 - 결제 번호 불일치")
    void validatePgResponseFail_paymentNumberMismatch() {
        // given
        Payment payment = PaymentFixture.payment(1L);
        PgPaymentResponse paymentRes = pgPaymentResponse("OTHER_PAYMENT_NUMBER", 10000L);

        // when & then
        assertThatThrownBy(() -> paymentService.validatePgResponse(payment, paymentRes))
            .isInstanceOf(ServiceException.class)
            .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.NOT_VALID_PG));
    }

    @Test
    @DisplayName("PG 응답 검증 실패 - 결제 금액 불일치")
    void validatePgResponseFail_amountMismatch() {
        // given
        Payment payment = PaymentFixture.payment(1L);
        PgPaymentResponse paymentRes = pgPaymentResponse(payment.getPaymentNumber(), 5000L);

        // when & then
        assertThatThrownBy(() -> paymentService.validatePgResponse(payment, paymentRes))
            .isInstanceOf(ServiceException.class)
            .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.NOT_VALID_PG));
    }


    // ========== 헬퍼 메서드 ==========

    private PgPaymentResponse pgPaymentResponse(String paymentNumber, Long totalAmount) {
        return new PgPaymentResponse(
            "mock-payment-key",
            paymentNumber,
            PaymentStatus.APPROVED,
            totalAmount,
            LocalDateTime.now().plusMinutes(1),
            "{mock raw response}",
            new PgCardResponse("11", "1234****", 0, false, "00000000")
        );
    }
}
