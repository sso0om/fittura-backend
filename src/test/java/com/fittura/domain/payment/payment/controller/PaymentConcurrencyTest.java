package com.fittura.domain.payment.payment.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.order.order.constant.OrderStatus;
import com.fittura.domain.order.order.entity.Order;
import com.fittura.domain.order.order.repository.OrderItemRepository;
import com.fittura.domain.order.order.repository.OrderRepository;
import com.fittura.domain.order.order.support.OrderFixture;
import com.fittura.domain.order.order.support.OrderItemFixture;
import com.fittura.domain.payment.facde.PaymentFacade;
import com.fittura.domain.payment.payment.constant.PaymentStatus;
import com.fittura.domain.payment.payment.dto.request.PaymentApproveReqDto;
import com.fittura.domain.payment.payment.entity.Payment;
import com.fittura.domain.payment.payment.repository.PaymentCardRepository;
import com.fittura.domain.payment.payment.repository.PaymentRepository;
import com.fittura.domain.payment.payment.support.PaymentFixture;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PaymentConcurrencyTest extends IntegrationTestBase {

    @Autowired private PaymentFacade paymentFacade;
    @Autowired private PaymentCardRepository paymentCardRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductSkuRepository skuRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = CategoryFixture.rootActive();
        categoryRepository.save(category);
    }

    @AfterEach
    void tearDown() {
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
        CountDownLatch readyLatch  = new CountDownLatch(threadCnt);
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
                        new  PaymentApproveReqDto(paymentKey)
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

        ProductSku result =skuRepository.findById(sku.getId()).orElseThrow();
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
        CountDownLatch startLatch  = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(tasks.size());
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

        ProductSku result =skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(result.getStockQuantity()).isEqualTo(initialStock - qtyA - qtyB);
        assertThat(result.getReservedQuantity()).isEqualTo(0);
    }


    // ========== 헬퍼 메서드 ==========

    private record PayTask(Long memberId, Long paymentId, String paymentKey) {}

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
        Order order = OrderFixture.order(memberId);
        OrderItemFixture.orderItem(order, sku, quantity);
        order.calcFinalAmount();

        sku.reserveQuantity(quantity);
        skuRepository.save(sku);

        return orderRepository.save(order);
    }

    private Payment createPayment(Order order) {
        Payment payment = PaymentFixture.payment(order.getId(), order.getFinalAmount());
        return paymentRepository.save(payment);
    }
}
