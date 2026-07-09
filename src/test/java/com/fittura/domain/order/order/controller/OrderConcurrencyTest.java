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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
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

        Product chair = ProductFixture.complete(category, "의자");
        chair.activate();
        productRepository.save(chair);
        ProductSku chairSku = ProductSkuFixture.sku(chair, 100_000L, initialStock, "RED", null);
        skuRepository.save(chairSku);

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

    private AddressCreateReqDto addressDto() {
        return new AddressCreateReqDto(
            "홍길동", "01012341234", "12345",
            "서울특별시 중구 서소문로 127", null, "서울특별시", "중구", null
        );
    }
}