package com.fittura.domain.order.order.controller;

import com.fittura.domain.category.entity.Category;
import com.fittura.domain.category.repository.CategoryRepository;
import com.fittura.domain.category.support.CategoryFixture;
import com.fittura.domain.order.cart.entity.Cart;
import com.fittura.domain.order.cart.entity.CartItem;
import com.fittura.domain.order.cart.repository.CartItemRepository;
import com.fittura.domain.order.cart.repository.CartRepository;
import com.fittura.domain.order.facade.OrderFacade;
import com.fittura.domain.order.order.dto.request.AddressCreateReqDto;
import com.fittura.domain.order.order.dto.request.OrderCreateReqDto;
import com.fittura.domain.order.order.repository.OrderAddressRepository;
import com.fittura.domain.order.order.repository.OrderItemRepository;
import com.fittura.domain.order.order.repository.OrderRepository;
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
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderConcurrencyTest extends IntegrationTestBase {

    @Autowired private OrderFacade orderFacade;
    @Autowired private OrderAddressRepository addressRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSkuRepository skuRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    private Category category;

    @BeforeEach
    void setUp() {
        category = CategoryFixture.rootActive();
        categoryRepository.save(category);
    }

    @AfterEach
    void tearDown() {
        cartItemRepository.deleteAll();
        addressRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        skuRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("재고보다 많은 동시 주문이 들어와도 초과 판매되지 않음")
    void concurrentOrdersDoNotExceedStock() throws InterruptedException {
        int initialStock = 3;
        int memberCnt = 5;

        ProductSku chairSku = getProductSku("의자", initialStock);

        List<Long> memberIds = LongStream.rangeClosed(1, memberCnt)
            .map(i -> 90000L + i)
            .boxed()
            .toList();

        List<Long> cartItemIds = new ArrayList<>();
        for (Long memberId : memberIds) {
            Cart cart = Cart.create(memberId);
            cartRepository.save(cart);

            CartItem item = CartItem.create(cart, chairSku, 1);
            cartItemRepository.save(item);
            cartItemIds.add(item.getId());
        }

        // ===== 동시 실행 =====
        ExecutorService executor = Executors.newFixedThreadPool(memberCnt);

        CountDownLatch readyLatch = new CountDownLatch(memberCnt);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(memberCnt);

        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCnt = new AtomicInteger();

        for (int i = 0; i < memberCnt; i++) {
            Long memberId = memberIds.get(i);
            Long cartItemId = cartItemIds.get(i);

            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    OrderCreateReqDto reqDto = new OrderCreateReqDto(
                        List.of(cartItemId),
                        0L,
                        addressDto()
                    );
                    orderFacade.createOrder(memberId, reqDto);
                    successCnt.incrementAndGet();
                } catch (Exception e) {
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

        ProductSku result = skuRepository.findById(chairSku.getId()).orElseThrow();
        assertThat(result.getReservedQuantity()).isLessThanOrEqualTo(result.getStockQuantity());
        assertThat(successCnt.get()).isEqualTo(initialStock);
        assertThat(failures).hasSize(memberCnt - initialStock);
    }

    @Test
    @DisplayName("서로 다른 순서로 담긴 카트를 동시 주문해도 데드락이 발생하지 않음")
    void concurrentOrdersWithReversedCartOrderDoNotDeadlock() throws InterruptedException {
        int stockEach = 5;

        ProductSku skuA = getProductSku("의자A", stockEach);
        ProductSku skuB = getProductSku("의자B", stockEach);

        Long memberX = 91001L;
        Long memberY = 91002L;

        // 회원 X: 카트에 [skuA, skuB] 순서로 담음
        Cart cartX = Cart.create(memberX);
        cartRepository.save(cartX);
        CartItem itemXA = CartItem.create(cartX, skuA, 1);
        cartItemRepository.save(itemXA);
        CartItem itemXB = CartItem.create(cartX, skuB, 1);
        cartItemRepository.save(itemXB);
        List<Long> cartItemIdsX = List.of(itemXA.getId(), itemXB.getId());

        // 회원 Y: 카트에 [skuB, skuA] 순서로 담음 (역순)
        Cart cartY = Cart.create(memberY);
        cartRepository.save(cartY);
        CartItem itemYB = CartItem.create(cartY, skuB, 1);
        cartItemRepository.save(itemYB);
        CartItem itemYA = CartItem.create(cartY, skuA, 1);
        cartItemRepository.save(itemYA);
        List<Long> cartItemIdsY = List.of(itemYB.getId(), itemYA.getId());

        List<OrderTask> tasks = List.of(
            new OrderTask(memberX, cartItemIdsX),
            new OrderTask(memberY, cartItemIdsY)
        );
        AddressCreateReqDto address = addressDto();

        // ===== 동시 실행 =====
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch readyLatch = new CountDownLatch(tasks.size());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(tasks.size());
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        for (OrderTask task : tasks) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    orderFacade.createOrder(
                        task.memberId(),
                        new OrderCreateReqDto(task.cartItemIds(), 0L, address)
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
                "failure type: " + e.getClass().getName()
                    + (e.getCause() != null ? " / cause: " + e.getCause().getClass().getName() : "")
            ));
        }

        boolean hasDeadlock = failures.stream().anyMatch(this::isDeadlockRelated);
        assertThat(hasDeadlock).isFalse();
    }

    @Test
    @DisplayName("같은 상품의 다른 SKU를 동시 주문해도 Product 락 경합 없이 독립적으로 처리됨")
    void concurrentOrdersOnSameProductDifferentSkuAreSerialized() throws Exception {
        long holdMillis = 2000L;

        Product chair = createActiveProduct("의자");
        ProductSku chairRed = createSku(chair, 5, "red");
        ProductSku chairBlue = createSku(chair, 5, "blue");

        Long memberX = 92001L;
        Long memberY = 92002L;

        Cart cartX = Cart.create(memberX);
        cartRepository.save(cartX);
        CartItem itemXRed = CartItem.create(cartX, chairRed, 1);
        cartItemRepository.save(itemXRed);

        Cart cartY = Cart.create(memberY);
        cartRepository.save(cartY);
        CartItem itemYBlue = CartItem.create(cartY, chairBlue, 1);
        cartItemRepository.save(itemYBlue);

        AddressCreateReqDto address = addressDto();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch lockAcquiredLatch = new CountDownLatch(1);

        // 스레드 A: chairRed 락을 잡고 holdMillis 동안 트랜잭션을 붙잡고 있음
        Future<?> threadA = executor.submit(() -> {
            transactionTemplate.execute(status -> {
                cartItemRepository.findAllWithSkuForUpdate(List.of(itemXRed.getId()), memberX);
                lockAcquiredLatch.countDown();
                try {
                    Thread.sleep(holdMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }); // execute()가 리턴하는 시점 = 트랜잭션 커밋 완료 = 락 해제
            return null;
        });

        // B: A가 락을 잡은 직후, 같은 Product의 다른 SKU(blue) 주문 시도 + 실행 시간 측정
        Future<Long> threadB = executor.submit(() -> {
            if (!lockAcquiredLatch.await(2, TimeUnit.SECONDS)) {
                // A가 신호를 못 줬다면, lockAcquiredLatch.countDown() 전에 예외로 끝났을 가능성이 높음
                if (threadA.isDone()) {
                    threadA.get();
                }
                throw new AssertionError("스레드 A가 락을 획득하지 못했습니다 (A가 아직 실행 중)");
            }
            long start = System.nanoTime();
            orderFacade.createOrder(memberY, new OrderCreateReqDto(List.of(itemYBlue.getId()), 0L, address));
            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        });

        // 안 끝나는 상황만 막는 안전장치
        Long executionTimeB = threadB.get(3, TimeUnit.SECONDS);

        // 락이 SKU 단위로 좁혀졌으므로, A가 다른 SKU의 락을 잡고 있어도 B는 대기 없이 완료해야 함
        assertThat(executionTimeB).isLessThan(holdMillis - 500L);

        threadA.get(10, TimeUnit.SECONDS);
        executor.shutdown();
    }


    // ========== 헬퍼 메서드 ==========

    private record OrderTask(Long memberId, List<Long> cartItemIds) {}

    private AddressCreateReqDto addressDto() {
        return new AddressCreateReqDto(
            "홍길동", "01012341234", "12345",
            "서울특별시 중구 서소문로 127", null, "서울특별시", "중구", null
        );
    }

    private Product createActiveProduct(String name) {
        Product product = ProductFixture.complete(category, name);
        product.activate();
        productRepository.save(product);
        return product;
    }

    private ProductSku createSku(Product product, int stock, String color) {
        ProductSku sku = ProductSkuFixture.sku(product, 100_000L, stock, color, null);
        skuRepository.save(sku);
        return sku;
    }

    private ProductSku getProductSku(String productName, int stock) {
        return createSku(createActiveProduct(productName), stock, null);
    }

    private boolean isDeadlockRelated(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof PessimisticLockingFailureException) {
                return true;
            }
        }
        return false;
    }
}