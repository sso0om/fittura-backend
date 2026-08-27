package com.fittura.domain.payment.payment.controller;

import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.repository.OrderRepository;
import com.fittura.domain.order.order.support.OrderFixture;
import com.fittura.domain.payment.payment.repository.PaymentRepository;
import com.fittura.global.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;

    private static final String PAYMENT_URL = "/api/v1/payments";

    // ========== 결제 준비 ==========

    @Test
    @DisplayName("결제 준비 성공")
    void preparePaymentSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Order order = createPendingOrder(memberId);

        String reqBody = """
                {
                    "orderId": %d,
                    "pgProvider": "TOSS",
                    "paymentMethod": "CARD"
                }
                """.formatted(order.getId());

        // when & then
        mockMvc.perform(post(PAYMENT_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("결제가 준비되었습니다."))
            .andExpect(jsonPath("$.data.orderId").value(order.getId()))
            .andExpect(jsonPath("$.data.pgProvider").value("TOSS"))
            .andExpect(jsonPath("$.data.paymentMethod").value("CARD"))
            .andExpect(jsonPath("$.data.totalAmount").value(order.getFinalAmount()));

        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("결제 준비 실패 - 존재하지 않는 주문")
    void preparePaymentFail_orderNotFound() throws Exception {
        // given
        Long memberId = 2L;

        String reqBody = """
                {
                    "orderId": 999999,
                    "pgProvider": "TOSS",
                    "paymentMethod": "CARD"
                }
                """;

        // when & then
        mockMvc.perform(post(PAYMENT_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(OrderErrorCode.NOT_FOUND_ORDER.getCode()));

        assertThat(paymentRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("결제 준비 실패 - 다른 회원의 주문으로 결제 시도")
    void preparePaymentFail_orderNotOwnedByMember() throws Exception {
        // given
        Long ownerMemberId = 3L;
        Long attackerMemberId = 4L;
        Order order = createPendingOrder(ownerMemberId);

        String reqBody = """
                {
                    "orderId": %d,
                    "pgProvider": "TOSS",
                    "paymentMethod": "CARD"
                }
                """.formatted(order.getId());

        // when & then
        mockMvc.perform(post(PAYMENT_URL)
                .header("Authorization", userBearerToken(attackerMemberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(OrderErrorCode.NOT_FOUND_ORDER.getCode()));

        assertThat(paymentRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("결제 준비 실패 - 결제 가능한 주문 상태가 아님")
    void preparePaymentFail_orderNotPayable() throws Exception {
        // given
        Long memberId = 5L;
        Order order = OrderFixture.order(memberId);
        order.prepare();
        orderRepository.save(order);

        String reqBody = """
                {
                    "orderId": %d,
                    "pgProvider": "TOSS",
                    "paymentMethod": "CARD"
                }
                """.formatted(order.getId());

        // when & then
        mockMvc.perform(post(PAYMENT_URL)
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(OrderErrorCode.NOT_PAYABLE_STATUS.getCode()));

        assertThat(paymentRepository.count()).isEqualTo(0);
    }


    // ========== 헬퍼 메서드 ==========

    private Order createPendingOrder(Long memberId) {
        Order order = OrderFixture.order(memberId);
        order.calcFinalAmount();
        return orderRepository.save(order);
    }
}