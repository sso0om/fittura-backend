package com.fittura.domain.payment.payment.entity;

import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.error.PaymentErrorCode;
import com.fittura.domain.payment.payment.support.PaymentFixture;
import com.fittura.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private static final String RAW_RESPONSE = "{\"code\":\"REJECT_CARD_COMPANY\"}";

    // ========== fail ==========

    @Test
    @DisplayName("fail 성공 - PENDING 결제를 FAILED로 변경하고 PG 응답 원문 저장")
    void failSuccess() {
        // given
        Payment payment = PaymentFixture.payment(1L);

        // when
        payment.fail(RAW_RESPONSE);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getRawResponse()).isEqualTo(RAW_RESPONSE);
    }

    @Test
    @DisplayName("fail 실패 - PENDING이 아닌 결제는 상태를 바꿀 수 없음")
    void failFail_notPending() {
        // given
        Payment payment = PaymentFixture.payment(1L);
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.APPROVED);

        // when & then
        assertThatThrownBy(() -> payment.fail(RAW_RESPONSE))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(PaymentErrorCode.NOT_PAYABLE_STATUS);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }


    // ========== unknown ==========

    @Test
    @DisplayName("unknown 성공 - PENDING 결제를 UNKNOWN으로 변경하고 PG 응답 원문 저장")
    void unknownSuccess() {
        // given
        Payment payment = PaymentFixture.payment(1L);

        // when
        payment.unknown(RAW_RESPONSE);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
        assertThat(payment.getRawResponse()).isEqualTo(RAW_RESPONSE);
    }

    @Test
    @DisplayName("unknown 실패 - PENDING이 아닌 결제는 상태를 바꿀 수 없음")
    void unknownFail_notPending() {
        // given
        Payment payment = PaymentFixture.payment(1L);
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.APPROVED);

        // when & then
        assertThatThrownBy(() -> payment.unknown(RAW_RESPONSE))
            .isInstanceOf(ServiceException.class)
            .extracting(e -> ((ServiceException) e).getErrorCode())
            .isEqualTo(PaymentErrorCode.NOT_PAYABLE_STATUS);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }
}
