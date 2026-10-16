package com.fittura.domain.payment.pg.toss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.constant.PgProvider;
import com.fittura.domain.payment.pg.PaymentGateway;
import com.fittura.domain.payment.pg.PgCardResponse;
import com.fittura.domain.payment.pg.PgConfirmCommand;
import com.fittura.domain.payment.pg.PgPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.ZoneId;
import java.util.Set;

@Slf4j
@Component
@Profile("toss")
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    private static final String CONFIRM_PATH = "/v1/payments/confirm";
    private static final String STATUS_DONE = "DONE";
    private static final String GENERIC_REJECT_MESSAGE = "결제 처리 중 오류가 발생했습니다";
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 토스 4xx 에러이지만 이미 승인됐거나 처리 중일 수 있는 코드 → 결과 불명으로 취급
    private static final Set<String> UNKNOWN_ON_4XX = Set.of(
        "ALREADY_PROCESSED_PAYMENT", "ALREADY_PROCESSING_REQUEST",
        "PROVIDER_ERROR", "CARD_PROCESSING_ERROR"
    );

    // 쇼핑몰 설정·요청 문제 → 고객에게 토스 원문 메시지를 보여주지 않음
    private static final Set<String> MERCHANT_SIDE_CODES = Set.of(
        "UNAUTHORIZED_KEY", "INVALID_API_KEY", "INCORRECT_BASIC_AUTH_FORMAT",
        "INVALID_REQUEST", "FORBIDDEN_REQUEST", "NOT_FOUND_TERMINAL_ID", "NOT_REGISTERED_BUSINESS",
        "INVALID_AUTHORIZE_AUTH", "INVALID_UNREGISTERED_SUBMALL"
    );

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    @Override
    public PgProvider pgProvider() {
        return PgProvider.TOSS;
    }

    @Override
    public PgPaymentResponse confirm(PgConfirmCommand command) {
        TossConfirmReqDto request = new TossConfirmReqDto(
            command.paymentKey(), command.paymentNumber(), command.amount()
        );

        try {
            String rawResponse = tossRestClient.post()
                .uri(CONFIRM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);

            return toResponse(command, rawResponse);
        } catch (RestClientResponseException e) {   // 토스가 4xx/5xx로 응답
            return fromErrorResponse(command, e);
        } catch (RestClientException e) {           // 타임아웃, 연결 실패 등 응답 자체를 못 받음
            log.warn("토스 승인 통신 실패 orderId={}", command.paymentNumber(), e);
            return unknown(command, null, "COMMUNICATION_ERROR");
        }
    }


    // ===== 응답 변환 =====

    private PgPaymentResponse toResponse(PgConfirmCommand command, String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            log.warn("토스 승인 응답 없음 orderId={}", command.paymentNumber());
            return unknown(command, rawResponse, "EMPTY_RESPONSE");
        }

        TossPaymentResDto response;
        try {
            response = objectMapper.readValue(rawResponse, TossPaymentResDto.class);
        } catch (JsonProcessingException e) {
            log.error("토스 승인 응답 파싱 실패 orderId={}", command.paymentNumber(), e);
            return unknown(command, rawResponse, "RESPONSE_PARSE_ERROR");
        }

        if (!STATUS_DONE.equals(response.status())) {
            log.warn("토스 승인 응답 상태 이상 orderId={} status={}", command.paymentNumber(), response.status());
            return unknown(command, rawResponse, "UNEXPECTED_STATUS");
        }

        return toApproved(response, rawResponse);
    }

    private PgPaymentResponse fromErrorResponse(PgConfirmCommand command, RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        HttpStatusCode status = e.getStatusCode();
        TossErrorResDto error = parseError(body);
        String code = error == null ? null : error.code();

        // 5xx, 에러 바디를 못 읽음, 승인됐을 수 있는 4xx → 결과 불명
        if (!status.is4xxClientError() || code == null || UNKNOWN_ON_4XX.contains(code)) {
            log.warn("토스 승인 결과 불명 orderId={} status={} code={}",
                command.paymentNumber(), status.value(), code);
            return unknown(command, body, code != null ? code : "HTTP_" + status.value());
        }

        // 우리 쪽 문제 → 추상 메시지로 거절, 원문은 로그로만
        if (MERCHANT_SIDE_CODES.contains(code)) {
            log.error("토스 승인 요청 오류(설정 확인 필요) orderId={}, code={}, message={}",
                command.paymentNumber(), code, error.message());
            return rejected(command, body, code, GENERIC_REJECT_MESSAGE);
        }

        log.warn("토스 승인 거절 orderId={} code={}, message={}",
            command.paymentNumber(), code, error.message());
        return rejected(command, body, code, error.message());
    }

    private TossErrorResDto parseError(String body) {
        try {
            return objectMapper.readValue(body, TossErrorResDto.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }


    // ===== 응답 결과 =====

    private PgPaymentResponse toApproved(TossPaymentResDto res, String rawResponse) {
        TossPaymentResDto.TossCardResDto card = res.card();

        return new PgPaymentResponse(
            res.paymentKey(),
            res.orderId(),
            PaymentStatus.APPROVED,
            res.totalAmount(),
            res.approvedAt().atZoneSameInstant(SEOUL).toLocalDateTime(),
            rawResponse,
            null,
            null,
            card == null ? null : new PgCardResponse(
                card.issuerCode(),
                card.number(),
                card.installmentPlanMonths(),
                Boolean.TRUE.equals(card.isInterestFree()),
                card.approveNo()
            )
        );
    }

    private PgPaymentResponse rejected(PgConfirmCommand command, String rawResponse, String code, String message) {
        return PgPaymentResponse.rejected(command.paymentKey(), command.paymentNumber(), rawResponse, code, message);
    }

    private PgPaymentResponse unknown(PgConfirmCommand command, String rawResponse, String code) {
        return PgPaymentResponse.unknown(command.paymentKey(), command.paymentNumber(), rawResponse, code);
    }
}
