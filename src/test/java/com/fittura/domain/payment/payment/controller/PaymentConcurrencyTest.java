package com.fittura.domain.payment.payment.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.order.order.constant.OrderStatus;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.repository.OrderItemRepository;
import com.fittura.domain.order.order.repository.OrderRepository;
import com.fittura.domain.order.order.support.OrderFixture;
import com.fittura.domain.payment.facade.PaymentFacade;
import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.dto.request.PaymentApproveReqDto;
import com.fittura.domain.payment.payment.entity.Payment;
import com.fittura.domain.payment.payment.error.PaymentErrorCode;
import com.fittura.domain.payment.payment.error.PgPaymentFailedException;
import com.fittura.domain.payment.payment.repository.PaymentCardRepository;
import com.fittura.domain.payment.payment.repository.PaymentRepository;
import com.fittura.domain.payment.payment.support.PaymentFixture;
import com.fittura.domain.payment.pg.mock.MockPaymentGateway;
import com.fittura.domain.product.product.entity.Product;
import com.fittura.domain.product.product.repository.ProductRepository;
import com.fittura.domain.product.product.support.ProductFixture;
import com.fittura.domain.product.sku.entity.ProductSku;
import com.fittura.domain.product.sku.repository.ProductSkuRepository;
import com.fittura.domain.product.sku.support.ProductSkuFixture;
import com.fittura.global.IntegrationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PaymentConcurrencyTest extends IntegrationTestBase {

    @Autowired
    private PaymentFacade paymentFacade;
    @Autowired
    private PaymentCardRepository paymentCardRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductSkuRepository skuRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MockPaymentGateway mockGateway;

    private Category category;

    @BeforeEach
    void setUp() {
        category = CategoryFixture.rootActive();
        categoryRepository.save(category);
    }

    @AfterEach
    void tearDown() {
        mockGateway.clear();
        paymentCardRepository.deleteAll();
        paymentRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        skuRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("동일 결제를 동시에 승인해도 한 건만 성공하고 재고는 한 번만 차감됨")
    void concurrentApproveSamePayment() throws Exception {
        int initialStock = 10;
        ProductSku sku = getProductSku("의자", initialStock);

        Long memberId = 93001L;
        int orderQty = 3;
        Order order = createOrderWithItem(memberId, sku, orderQty);

        String paymentKey = "key-single";
        Payment payment = createPayment(order);

        // ===== 동시 실행 =====
        int threadCnt = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCnt);
        CountDownLatch readyLatch = new CountDownLatch(threadCnt);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCnt);
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCnt = new AtomicInteger();

        for (int i = 0; i < threadCnt; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    paymentFacade.approvePayment(
                        memberId,
                        payment.getId(),
                        new PaymentApproveReqDto(paymentKey)
                    );
                    successCnt.incrementAndGet();
                } catch (Throwable e) {
                    failures.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // ===== 검증 =====
        assertThat(finished).isTrue();

        if (!failures.isEmpty()) {
            failures.forEach(e -> System.out.println(
                "failure: " + e.getClass().getName()
                    + (e.getCause() != null ? " / cause: " + e.getCause().getClass().getName() : "")));
        }

        assertThat(successCnt.get()).isEqualTo(1);
        assertThat(failures.size()).isEqualTo(threadCnt - 1);

        Payment approved = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(approved.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        Order paidOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        ProductSku result = skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(result.getStockQuantity()).isEqualTo(initialStock - orderQty);
        assertThat(result.getReservedQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("서로 다른 결제가 같은 SKU를 동시 승인해도 재고가 정확히 차감됨")
    void concurrentConfirmOnSameSku() throws Exception {
        int initialStock = 10;
        ProductSku sku = getProductSku("의자", initialStock);

        Long memberA = 94001L;
        Long memberB = 94002L;
        int qtyA = 3;
        int qtyB = 2;

        Order orderA = createOrderWithItem(memberA, sku, qtyA);
        Order orderB = createOrderWithItem(memberB, sku, qtyB);

        String keyA = "key-A";
        String keyB = "key-B";
        Payment paymentA = createPayment(orderA);
        Payment paymentB = createPayment(orderB);

        List<PayTask> tasks = List.of(
            new PayTask(memberA, paymentA.getId(), keyA),
            new PayTask(memberB, paymentB.getId(), keyB)
        );

        // ===== 동시 실행 =====
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch readyLatch = new CountDownLatch(tasks.size());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(tasks.size());
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        for (PayTask task : tasks) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    paymentFacade.approvePayment(
                        task.memberId(),
                        task.paymentId(),
                        new PaymentApproveReqDto(task.paymentKey)
                    );
                } catch (Throwable e) {
                    failures.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // ===== 검증 =====
        assertThat(finished).isTrue();

        if (!failures.isEmpty()) {
            failures.forEach(e -> System.out.println(
                "failure: " + e.getClass().getName()
                    + (e.getCause() != null ? " / cause: " + e.getCause().getClass().getName() : "")));
        }
        assertThat(failures).isEmpty();

        ProductSku result = skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(result.getStockQuantity()).isEqualTo(initialStock - qtyA - qtyB);
        assertThat(result.getReservedQuantity()).isEqualTo(0);
    }


    @Test
    @DisplayName("한 결제는 거절, 다른 결제는 승인되어도 같은 SKU의 재고와 예약이 정확함")
    void concurrentRejectAndConfirmOnSameSku() throws Exception {
        int initialStock = 10;
        ProductSku sku = getProductSku("의자", initialStock);

        Long memberA = 95001L;
        Long memberB = 95002L;
        int qtyA = 3;   // 거절
        int qtyB = 2;   // 승인

        Order orderA = createOrderWithItem(memberA, sku, qtyA);
        Order orderB = createOrderWithItem(memberB, sku, qtyB);
        Payment paymentA = createPayment(orderA);
        Payment paymentB = createPayment(orderB);
        mockGateway.stubRejected("key-A", "REJECT_CARD_COMPANY", "카드사 거절");

        // ===== 동시 실행 =====
        List<Throwable> failures = runConcurrently(List.of(
            () -> paymentFacade.approvePayment(memberA, paymentA.getId(), new PaymentApproveReqDto("key-A")),
            () -> paymentFacade.approvePayment(memberB, paymentB.getId(), new PaymentApproveReqDto("key-B"))
        ));

        // ===== 검증 =====
        // 거절 요청만 예외, 승인 요청은 성공
        assertThat(failures).hasSize(1);
        assertThat(failures.getFirst()).isInstanceOfSatisfying(PgPaymentFailedException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(PaymentErrorCode.PG_REJECTED));

        // 거절된 예약(3)은 해제되고 승인된 수량(2)만 재고에서 차감
        ProductSku result = skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(result.getStockQuantity()).isEqualTo(initialStock - qtyB);
        assertThat(result.getReservedQuantity()).isEqualTo(0);

        // 실제 커밋 확인 (noRollbackFor)
        assertThat(paymentRepository.findById(paymentA.getId()).orElseThrow().getStatus())
            .isEqualTo(PaymentStatus.FAILED);
        assertThat(orderRepository.findById(orderA.getId()).orElseThrow().getStatus())
            .isEqualTo(OrderStatus.EXPIRED);
        assertThat(orderRepository.findById(orderB.getId()).orElseThrow().getStatus())
            .isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("동일 결제를 동시에 거절 처리해도 예약은 한 번만 해제됨")
    void concurrentRejectSamePayment() throws Exception {
        int initialStock = 10;
        ProductSku sku = getProductSku("의자", initialStock);

        Long memberId = 96001L;
        int qty = 3;
        int otherQty = 4;   // 다른 주문의 예약 - 중복 해제되면 이 값이 깎임

        Order order = createOrderWithItem(memberId, sku, qty);
        createOrderWithItem(96002L, sku, otherQty);
        Payment payment = createPayment(order);
        mockGateway.stubRejected("key-reject", "REJECT_CARD_COMPANY", "카드사 거절");

        // ===== 동시 실행 =====
        int threadCnt = 5;
        Runnable approve = () -> paymentFacade.approvePayment(
            memberId, payment.getId(), new PaymentApproveReqDto("key-reject"));
        List<Throwable> failures = runConcurrently(Collections.nCopies(threadCnt, approve));

        // ===== 검증 =====
        // 전원 예외: 한 건은 거절 처리, 나머지는 이미 처리된 결제
        assertThat(failures).hasSize(threadCnt);
        assertThat(failures.stream().filter(PgPaymentFailedException.class::isInstance).count())
            .isEqualTo(1);

        ProductSku result = skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(result.getStockQuantity()).isEqualTo(initialStock);
        assertThat(result.getReservedQuantity()).isEqualTo(otherQty);
    }

    @Test
    @DisplayName("PG 결과 불명이면 결제만 UNKNOWN으로 커밋되고 주문과 예약은 유지됨")
    void unknownKeepsOrderAndReservation() {
        int initialStock = 10;
        ProductSku sku = getProductSku("의자", initialStock);

        Long memberId = 97001L;
        int qty = 3;
        Order order = createOrderWithItem(memberId, sku, qty);
        Payment payment = createPayment(order);
        mockGateway.stubUnknown("key-unknown", "FAILED_INTERNAL_SYSTEM_PROCESSING");

        // when & then
        assertThatThrownBy(() -> paymentFacade.approvePayment(
            memberId, payment.getId(), new PaymentApproveReqDto("key-unknown")))
            .isInstanceOfSatisfying(PgPaymentFailedException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(PaymentErrorCode.PG_RESULT_UNKNOWN));

        // 실제 커밋 확인 (noRollbackFor) + 주문·예약 유지
        assertThat(paymentRepository.findById(payment.getId()).orElseThrow().getStatus())
            .isEqualTo(PaymentStatus.UNKNOWN);
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus())
            .isEqualTo(OrderStatus.PENDING);

        ProductSku result = skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(result.getStockQuantity()).isEqualTo(initialStock);
        assertThat(result.getReservedQuantity()).isEqualTo(qty);
    }


    // ========== 헬퍼 메서드 ==========

    private record PayTask(Long memberId, Long paymentId, String paymentKey) {
    }

    // 모든 작업을 동시에 시작시키고, 발생한 예외를 모아서 반환
    private List<Throwable> runConcurrently(List<Runnable> tasks) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch readyLatch = new CountDownLatch(tasks.size());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(tasks.size());
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        for (Runnable task : tasks) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    task.run();
                } catch (Throwable e) {
                    failures.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).isTrue();
        return failures;
    }

    private ProductSku getProductSku(String name, int stock) {
        return createSku(createActiveProduct(name), stock);
    }

    private Product createActiveProduct(String name) {
        Product product = ProductFixture.complete(category, name);
        product.activate();
        productRepository.save(product);
        return product;
    }

    private ProductSku createSku(Product product, int stock) {
        ProductSku sku = ProductSkuFixture.sku(product, 100_000L, stock);
        skuRepository.save(sku);
        return sku;
    }

    private Order createOrderWithItem(Long memberId, ProductSku sku, Integer quantity) {
        Order order = OrderFixture.orderWithItem(memberId, 0L, sku, quantity);

        sku.reserveQuantity(quantity);
        skuRepository.save(sku);

        return orderRepository.save(order);
    }

    private Payment createPayment(Order order) {
        Payment payment = PaymentFixture.payment(order.getId(), order.getFinalAmount());
        return paymentRepository.save(payment);
    }
}
