package com.fittura.domain.payment.payment.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.repository.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository;
import com.fittura.domain.order.cart.support.CartFixture;
import com.fittura.domain.order.cart.support.CartItemFixture;
import com.fittura.domain.order.order.constant.OrderStatus;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.error.OrderErrorCode;
import com.fittura.domain.order.order.repository.OrderRepository;
import com.fittura.domain.order.order.support.OrderFixture;
import com.fittura.domain.order.order.support.OrderItemFixture;
import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.entity.Payment;
import com.fittura.domain.payment.payment.error.PaymentErrorCode;
import com.fittura.domain.payment.payment.repository.PaymentRepository;
import com.fittura.domain.payment.payment.support.PaymentFixture;
import com.fittura.domain.payment.pg.MockPaymentGateway;
import com.fittura.domain.payment.pg.PgCardResponse;
import com.fittura.domain.payment.pg.PgPaymentResponse;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import com.fittura.global.IntegrationTestBase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSkuRepository productSkuRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private MockPaymentGateway mockGateway;

    private static final String PAYMENT_URL = "/api/v1/payments";
    private static final String PAYMENT_KEY = "MOCK_PAYMENT_NUMBER_123";

    @AfterEach
    void clearStubs() {
        mockGateway.clear();
    }

    // ========== 결제 준비 ==========

    @Test
    @DisplayName("결제 준비 성공")
    void preparePaymentSuccess() throws Exception {
        // given
        Long memberId = 1L;
        ProductSku sku = savedDefaultSku();
        Order order = createOrderWithItem(memberId, sku, 10);

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
        ProductSku sku = savedDefaultSku();
        Order order = createOrderWithItem(ownerMemberId, sku, 10);

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
        ProductSku sku = savedDefaultSku();
        Order order = createOrderWithItem(memberId, sku, 10);
        order.prepare();

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


    // ========== 결제 승인 ==========

    @Test
    @DisplayName("결제 승인 성공")
    void approvePaymentSuccess() throws Exception {
        // given
        Long memberId = 6L;
        ProductSku sku = savedSkuWithPrice(7000L, 100);
        ProductSku sku2 = savedSkuWithPrice(7000L, 100);
        Cart cart = cartRepository.save(CartFixture.cart(memberId));
        cartItemRepository.save(CartItemFixture.cartItem(cart, sku, 1));
        cartItemRepository.save(CartItemFixture.cartItem(cart, sku2, 2));

        Order order = createOrderWithItem(memberId, sku, 1);
        Payment payment = savedPayment(order);

        String reqBody = """
                {
                    "paymentKey": "%s"
                }
                """.formatted(PAYMENT_KEY);

        // when & then
        mockMvc.perform(post(PAYMENT_URL + "/" + payment.getId())
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("S201-01"))
            .andExpect(jsonPath("$.message").value("결제가 완료되었습니다."))
            .andExpect(jsonPath("$.data").value(order.getId()));

        entityManager.flush();
        entityManager.clear();

        Payment approvedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(approvedPayment.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        Order paidOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        ProductSku updatedSku = productSkuRepository.findById(sku.getId()).orElseThrow();
        assertThat(updatedSku.getStockQuantity()).isEqualTo(99);

        assertThat(cartItemRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("결제 승인 실패 - 존재하지 않는 결제")
    void approvePaymentFail_paymentNotFound() throws Exception {
        // given
        Long memberId = 7L;

        String reqBody = """
                {
                    "paymentKey": "%s"
                }
                """.formatted(PAYMENT_KEY);

        // when & then
        mockMvc.perform(post(PAYMENT_URL + "/999999")
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(PaymentErrorCode.NOT_FOUND_PAYMENT.getCode()));
    }

    @Test
    @DisplayName("결제 승인 실패 - 결제 가능한 상태가 아님")
    void approvePaymentFail_paymentNotPayable() throws Exception {
        // given
        Long memberId = 8L;
        ProductSku sku = savedSkuWithPrice(7000L, 100);
        Order order = createOrderWithItem(memberId, sku, 1);
        Payment payment = savedPayment(order);
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.APPROVED);

        String reqBody = """
                {
                    "paymentKey": "%s"
                }
                """.formatted(PAYMENT_KEY);

        // when & then
        mockMvc.perform(post(PAYMENT_URL + "/" + payment.getId())
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(PaymentErrorCode.NOT_PAYABLE_STATUS.getCode()));
    }

    @Test
    @DisplayName("결제 승인 실패 - PG 응답과 결제 정보 불일치")
    void approvePaymentFail_pgResponseMismatch() throws Exception {
        // given
        Long memberId = 9L;
        ProductSku sku = savedSkuWithPrice(7000L, 100);
        Order order = createOrderWithItem(memberId, sku, 1);
        Payment payment = savedPayment(order);
        mockGateway.stub(PAYMENT_KEY,
            pgPaymentResponse(PAYMENT_KEY, payment.getPaymentNumber(), order.getFinalAmount() + 10000L));

        String reqBody = """
                {
                    "paymentKey": "%s"
                }
                """.formatted(PAYMENT_KEY);

        // when & then
        mockMvc.perform(post(PAYMENT_URL + "/" + payment.getId())
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(PaymentErrorCode.NOT_VALID_PG.getCode()));

        entityManager.clear();

        ProductSku untouchedSku = productSkuRepository.findById(sku.getId()).orElseThrow();
        assertThat(untouchedSku.getStockQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("결제 승인 실패 - 다른 회원의 주문으로 결제 승인 시도")
    void approvePaymentFail_orderNotOwnedByMember() throws Exception {
        // given
        Long ownerMemberId = 10L;
        Long attackerMemberId = 11L;
        ProductSku sku = savedSkuWithPrice(7000L, 100);
        Order order = createOrderWithItem(ownerMemberId, sku, 1);
        Payment payment = savedPayment(order);

        String reqBody = """
                {
                    "paymentKey": "%s"
                }
                """.formatted(PAYMENT_KEY);

        // when & then
        mockMvc.perform(post(PAYMENT_URL + "/" + payment.getId())
                .header("Authorization", userBearerToken(attackerMemberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(OrderErrorCode.NOT_FOUND_ORDER.getCode()));
    }

    @Test
    @DisplayName("결제 승인 실패 - 결제 가능한 주문 상태가 아님")
    void approvePaymentFail_orderNotPayable() throws Exception {
        // given
        Long memberId = 12L;
        ProductSku sku = savedSkuWithPrice(7000L, 100);
        Order order = createOrderWithItem(memberId, sku, 1);
        Payment payment = savedPayment(order);
        order.prepare();

        String reqBody = """
                {
                    "paymentKey": "%s"
                }
                """.formatted(PAYMENT_KEY);

        // when & then
        mockMvc.perform(post(PAYMENT_URL + "/" + payment.getId())
                .header("Authorization", userBearerToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(OrderErrorCode.NOT_PAYABLE_STATUS.getCode()));
    }


    // ========== 헬퍼 메서드 ==========

    private ProductSku savedDefaultSku() {
        return savedSkuWithPrice(10000L, 100);
    }

    private ProductSku savedSkuWithPrice(Long price, Integer stock) {
        Category category = categoryRepository.save(CategoryFixture.rootActive());
        Product product = productRepository.save(ProductFixture.component(category, "A Desk"));
        product.activate();
        return productSkuRepository.save(ProductSkuFixture.sku(product, price, stock));
    }

    private Order createOrderWithItem(Long memberId, ProductSku sku, Integer quantity) {
        Order order = OrderFixture.order(memberId, 1000L);
        OrderItemFixture.orderItem(order, sku, quantity);
        order.calcFinalAmount();

        sku.reserveQuantity(quantity);

        return orderRepository.save(order);
    }

    private Payment savedPayment(Order order) {
        Payment payment = PaymentFixture.payment(order.getId(), order.getFinalAmount());
        return paymentRepository.save(payment);
    }

    private Payment savedPaymentMatchingPg(Order order, String paymentKey) {
        Payment payment = savedPayment(order);
        ReflectionTestUtils.setField(payment, "paymentNumber", paymentKey);
        mockGateway.stub(paymentKey, pgPaymentResponse(paymentKey, payment.getPaymentNumber(), order.getFinalAmount()));
        return payment;
    }

    private Payment savedPaymentMatchingPg(Order order) {
        return savedPaymentMatchingPg(order, PAYMENT_KEY);
    }

    private PgPaymentResponse pgPaymentResponse(String paymentKey, String paymentNumber, Long totalAmount) {
        return new PgPaymentResponse(
            paymentKey,
            paymentNumber,
            PaymentStatus.APPROVED,
            totalAmount,
            LocalDateTime.now().plusMinutes(1),
            "{mock raw response}",
            new PgCardResponse("11", "1234****", 0, false, "00000000")
        );
    }
}