package com.fittura.domain.payment.pg.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.pg.PgConfirmCommand;
import com.fittura.domain.payment.pg.PgPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TossPaymentGatewayTest {

    private static final String BASE_URL = "https://api.tosspayments.com";
    private static final String CONFIRM_URL = BASE_URL + "/v1/payments/confirm";
    private static final PgConfirmCommand COMMAND =
        new PgConfirmCommand("pay_key_1", "PAY-20261015-abc", 10000L);

    private MockRestServiceServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        gateway = new TossPaymentGateway(builder.build(), objectMapper);
    }

    @Test
    @DisplayName("승인 성공 - 요청 바디를 보내고 응답을 승인 결과로 변환한다")
    void confirmApproved() {
        // given
        server.expect(requestTo(CONFIRM_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.paymentKey").value("pay_key_1"))
            .andExpect(jsonPath("$.orderId").value("PAY-20261015-abc"))
            .andExpect(jsonPath("$.amount").value(10000))
            .andRespond(withSuccess("""
                {
                  "paymentKey": "pay_key_1",
                  "orderId": "PAY-20261015-abc",
                  "status": "DONE",
                  "totalAmount": 10000,
                  "approvedAt": "2026-10-15T10:30:00+09:00",
                  "card": {
                    "issuerCode": "61",
                    "number": "12345678****123*",
                    "installmentPlanMonths": 0,
                    "isInterestFree": true,
                    "approveNo": "00000000"
                  }
                }
                """, MediaType.APPLICATION_JSON));

        // when
        PgPaymentResponse result = gateway.confirm(COMMAND);

        // then
        server.verify();
        assertThat(result.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(result.paymentKey()).isEqualTo("pay_key_1");
        assertThat(result.paymentNumber()).isEqualTo("PAY-20261015-abc");
        assertThat(result.totalAmount()).isEqualTo(10000L);
        assertThat(result.approvedDate()).isEqualTo(LocalDateTime.of(2026, 10, 15, 10, 30));
        assertThat(result.card().issuerCode()).isEqualTo("61");
        assertThat(result.card().cardNumberMasked()).isEqualTo("12345678****123*");
        assertThat(result.card().installmentMonths()).isZero();
        assertThat(result.card().interestFree()).isTrue();
        assertThat(result.card().approvalNumber()).isEqualTo("00000000");
    }

    @Test
    @DisplayName("고객 사유 거절 - FAILED, 토스 원문 메시지를 그대로 전달한다")
    void confirmRejectedByCustomerReason() {
        // given
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"code": "REJECT_CARD_COMPANY", "message": "카드사에서 결제를 거절했습니다."}
                    """));

        // when
        PgPaymentResponse result = gateway.confirm(COMMAND);

        // then
        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.errorCode()).isEqualTo("REJECT_CARD_COMPANY");
        assertThat(result.errorMessage()).isEqualTo("카드사에서 결제를 거절했습니다.");
    }

    @Test
    @DisplayName("쇼핑몰 설정 문제 거절 - FAILED, 토스 원문 대신 추상 메시지를 전달한다")
    void confirmRejectedByMerchantSideCode() {
        // given
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"code": "INVALID_API_KEY", "message": "잘못된 시크릿키 연동 정보 입니다."}
                    """));

        // when
        PgPaymentResponse result = gateway.confirm(COMMAND);

        // then
        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.errorCode()).isEqualTo("INVALID_API_KEY");
        assertThat(result.errorMessage()).isEqualTo("결제 처리 중 오류가 발생했습니다");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unknownCases")
    @DisplayName("결과 불명 - 절대 FAILED로 처리하지 않고 UNKNOWN으로 기록한다")
    void confirmUnknown(String caseName, ResponseCreator response, String expectedErrorCode) {
        // given
        server.expect(requestTo(CONFIRM_URL)).andRespond(response);

        // when
        PgPaymentResponse result = gateway.confirm(COMMAND);

        // then
        assertThat(result.status()).isEqualTo(PaymentStatus.UNKNOWN);
        assertThat(result.errorCode()).isEqualTo(expectedErrorCode);
        assertThat(result.errorMessage()).isNull();
    }

    static Stream<Arguments> unknownCases() {
        return Stream.of(
            Arguments.of("통신 실패(타임아웃·연결 실패)",
                withException(new IOException("timeout")), "COMMUNICATION_ERROR"),
            Arguments.of("4xx지만 이미 처리됐을 수 있는 코드",
                errorBody(HttpStatus.BAD_REQUEST, """
                    {"code": "ALREADY_PROCESSED_PAYMENT", "message": "이미 처리된 결제 입니다."}
                    """), "ALREADY_PROCESSED_PAYMENT"),
            Arguments.of("5xx 에러 응답",
                errorBody(HttpStatus.INTERNAL_SERVER_ERROR, """
                    {"code": "FAILED_INTERNAL_SYSTEM_PROCESSING", "message": "내부 시스템 처리 작업이 실패했습니다."}
                    """), "FAILED_INTERNAL_SYSTEM_PROCESSING"),
            Arguments.of("5xx이고 에러 바디 없음",
                withStatus(HttpStatus.BAD_GATEWAY), "HTTP_502"),
            Arguments.of("4xx이고 에러 바디를 읽을 수 없음",
                errorBody(HttpStatus.BAD_REQUEST, "not json"), "HTTP_400"),
            Arguments.of("200인데 응답 바디 없음",
                withSuccess("", MediaType.APPLICATION_JSON), "EMPTY_RESPONSE"),
            Arguments.of("200인데 응답 JSON 파싱 불가",
                withSuccess("not json", MediaType.APPLICATION_JSON), "RESPONSE_PARSE_ERROR"),
            Arguments.of("200인데 상태가 DONE이 아님",
                withSuccess("""
                    {"paymentKey": "pay_key_1", "orderId": "PAY-20261015-abc", "status": "WAITING_FOR_DEPOSIT"}
                    """, MediaType.APPLICATION_JSON), "UNEXPECTED_STATUS"),
            Arguments.of("200 DONE인데 승인 시각 누락",
                withSuccess("""
                    {"paymentKey": "pay_key_1", "orderId": "PAY-20261015-abc", "status": "DONE", "totalAmount": 10000}
                    """, MediaType.APPLICATION_JSON), "INCOMPLETE_RESPONSE")
        );
    }

    private static ResponseCreator errorBody(HttpStatus status, String body) {
        return withStatus(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
